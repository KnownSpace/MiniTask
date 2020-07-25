package org.knownspace.minitask.locks;

//never use to copy
public class Unlocker<Lock extends Unlockable> implements AutoCloseable {

    Lock _lock;

    boolean _locked;

    public Unlocker(Lock lock) {
        _lock = lock;
        _locked = true;
    }

    public void cancel() {
        _locked = false;
    }

    public void unlock() {
        try {
            close();
        } catch (Exception ignore) {
            
        }
    }

    public void reset() {
        _locked = true;
    }

    @Override
    public void close() throws Exception {
        if(_locked)
        {
            _lock.unlock();
        }
    }
}