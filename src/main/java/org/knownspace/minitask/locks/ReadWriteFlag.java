package org.knownspace.minitask.locks;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.knownspace.minitask.TaskHelper;
import org.knownspace.minitask.ITask;
import org.knownspace.minitask.ITaskCompletionEvent;
import org.knownspace.minitask.TaskCompletionEvent;

public class ReadWriteFlag implements IUnlockable {

    private ReadWriteFlagState _state;

    private List<ITaskCompletionEvent<Void>> _waitReaders;

    private List<ITaskCompletionEvent<Void>> _waitWriters;

    private int _readerRef;

    private SpinLock _lock;

    private ExecutorService _executor;

    public ReadWriteFlag(ExecutorService executor) {
        _state = ReadWriteFlagState.FREE;
        _waitReaders = new LinkedList<>();
        _waitWriters = new LinkedList<>();
        _readerRef = 0;
        _lock = new SpinLock();
        _executor = executor;
    }

    public ITask<Void> lockRead() {
        ITaskCompletionEvent<Void> ce = new TaskCompletionEvent<>(_executor);
        _lock.lock();
        try (Unlocker<SpinLock> raiiUnlocker = new Unlocker<SpinLock>(_lock)){
            if(_state != ReadWriteFlagState.WRITE){
                _state = ReadWriteFlagState.READ;
                _readerRef += 1;
                raiiUnlocker.unlock();
                ce.complete(TaskHelper.voidValue);
            } else {
                _waitReaders.add(ce);
            }
        } catch (Exception ignore) {}
        return ce.getTask();
    }

    public ITask<Void> lockWrite() {
        ITaskCompletionEvent<Void> ce = new TaskCompletionEvent<>(_executor);
        _lock.lock();
        try (Unlocker<SpinLock> raiiUnlocker = new Unlocker<SpinLock>(_lock)) {
            if(_state == ReadWriteFlagState.FREE) {
                _state = ReadWriteFlagState.WRITE;
                raiiUnlocker.unlock();
                ce.complete(TaskHelper.voidValue);
            } else {
                _waitWriters.add(ce);
            }
        } catch (Exception ignore) {}
        return ce.getTask();
    }

    @Override
    public void unlock() {
        _lock.lock();
        try (Unlocker<SpinLock> raiiUnlocker = new Unlocker<SpinLock>(_lock)) {
            if(_state == ReadWriteFlagState.READ) {
                _readerRef -= 1;
                if(_readerRef == 0) {
                    if(!_waitWriters.isEmpty()) {
                        _state = ReadWriteFlagState.WRITE;
                        ITaskCompletionEvent<Void> ce = _waitWriters.get(0);
                        _waitWriters.remove(0);
                        raiiUnlocker.unlock();
                        ce.complete(TaskHelper.voidValue);
                    } else {
                        _state = ReadWriteFlagState.FREE;
                    }
                }
            } else if(_state == ReadWriteFlagState.WRITE) {
                if(!_waitWriters.isEmpty()) {
                    ITaskCompletionEvent<Void> ce = _waitWriters.get(0);
                    _waitWriters.remove(0);
                    raiiUnlocker.unlock();
                    ce.complete(TaskHelper.voidValue);
                } else if(!_waitReaders.isEmpty()) {
                    List<ITaskCompletionEvent<Void>> waits = new LinkedList<>();
                    List<ITaskCompletionEvent<Void>> tmp = waits;
                    waits = _waitReaders;
                    _waitReaders = tmp;
                    _readerRef += waits.size();
                    _state = ReadWriteFlagState.READ;
                    raiiUnlocker.unlock();
                    for (ITaskCompletionEvent<Void> ce : waits) {
                        ce.complete(TaskHelper.voidValue);
                    }
                } else {
                    _state = ReadWriteFlagState.FREE;
                }
            }
        } catch (Exception ignore) {}
    }
    
}