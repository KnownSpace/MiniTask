package org.knownspace.minitask.locks;

import java.util.concurrent.atomic.AtomicBoolean;

public class SpinLock implements IUnlockable {
    private AtomicBoolean _locked;

    public SpinLock()
    {
        _locked = new AtomicBoolean(false);
    }

    public void lock()
    {
        while(!_locked.compareAndSet(false,true))
        {
            Thread.yield();
        }
    }

    public void unlock()
    {
        _locked.set(false);
    }
}