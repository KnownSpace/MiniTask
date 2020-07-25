package org.knownspace.minitask.locks;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.knownspace.minitask.Helper;
import org.knownspace.minitask.ITask;
import org.knownspace.minitask.ITaskCompletionEvent;
import org.knownspace.minitask.TaskCompletionEvent;

public class UniqueFlag implements IUnlockable {

    private SpinLock _lock;

    private List<ITaskCompletionEvent<Void>> _waits;

    private boolean _locked;

    private ExecutorService _executor;

    public UniqueFlag(ExecutorService executor)
    {
        _lock = new SpinLock();
        _waits = new LinkedList<>();
        _locked = false;
        _executor = executor;
    }

    public ITask<Void> lock()
    {
        ITaskCompletionEvent<Void> ce = new TaskCompletionEvent<>(_executor);
        _lock.lock();
        try (Unlocker<SpinLock> raiiUnlocker = new Unlocker<SpinLock>(_lock)) {
            if(!_locked) {
                _locked = true;
                raiiUnlocker.unlock();
                ce.complete(Helper.voidValue);
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
            if(_waits.isEmpty()) {
                _locked = false;
            } else {
                ITaskCompletionEvent<Void> ce = _waits.get(0);
                _waits.remove(0);
                raiiUnlocker.unlock();
                ce.complete(Helper.voidValue);
            }
        } catch (Exception ignore) {}
    }
}