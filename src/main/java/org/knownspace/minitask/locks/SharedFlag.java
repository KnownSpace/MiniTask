package org.knownspace.minitask.locks;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.knownspace.minitask.TaskHelper;
import org.knownspace.minitask.ITask;

import org.knownspace.minitask.ITaskCompletionEvent;
import org.knownspace.minitask.TaskCompletionEvent;

public class SharedFlag implements IUnlockable {

    private volatile int _count;

    private List<ITaskCompletionEvent<Void>> _waits;

    private SpinLock _lock;

    private ExecutorService _executor;

    public SharedFlag(int count,ExecutorService executor) {
        if(count < 1) {
            throw new IllegalArgumentException("count should not lower than 1");
        }
        _count = count;
        _waits = new LinkedList<>();
        _lock = new SpinLock();
        _executor = executor;
    }

    public ITask<Void> lock() {
        ITaskCompletionEvent<Void> ce = new TaskCompletionEvent<>(_executor);
        _lock.lock();
        try (Unlocker<SpinLock> raiiUnLocker = new Unlocker<>(_lock)) {
            if(_count  > 0) {
                _count -=1;
                raiiUnLocker.unlock();
                ce.complete(TaskHelper.voidValue);
            } else {
                _waits.add(ce);
            }
        } catch (Exception ignore) {}
        return ce.getTask();
    }

    @Override
    public void unlock() {
        _lock.lock();
        try (Unlocker<SpinLock> raiiUnlocker = new Unlocker<SpinLock>(_lock)) {
            if(_waits.isEmpty())
            {
                _count +=1;
                return;
            }
            ITaskCompletionEvent<Void> ce = _waits.get(0);
            _waits.remove(0);
            raiiUnlocker.unlock();
            ce.complete(TaskHelper.voidValue);
        } catch (Exception ignore) {}
    }
    
}