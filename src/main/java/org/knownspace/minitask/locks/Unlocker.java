package org.knownspace.minitask.locks;

//never use to copy
//只在try-with-resource使用它
//且不能用于传递引用
//RAII解锁器
public class Unlocker<Lock extends IUnlockable> implements AutoCloseable {

    //实现Unlockable的锁
    private Lock _lock;

    //指示是否锁定
    private boolean _locked;

    public Unlocker(Lock lock) {
        _lock = lock;
        _locked = true;
    }

    //取消操作
    public void cancel() {
        _locked = false;
    }

    //手动解锁
    public void unlock() {
        try {
            close();
        } catch (Exception ignore) {
            
        }
    }

    //重设
    public void reset() {
        _locked = true;
    }

    @Override
    public void close() throws Exception {
        if(_locked)
        {
            _lock.unlock();
            _locked = false;
        }
    }
}