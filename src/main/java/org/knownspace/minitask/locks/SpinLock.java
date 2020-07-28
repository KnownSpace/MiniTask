package org.knownspace.minitask.locks;

import java.util.concurrent.atomic.AtomicBoolean;

//自选锁
public class SpinLock implements IUnlockable {
    //指示是否锁定
    private AtomicBoolean _locked;

    public SpinLock()
    {
        _locked = new AtomicBoolean(false);
    }

    //加锁
    public void lock()
    {
        //CAS算法
        while(!_locked.compareAndSet(false,true))
        {
            //不能获得锁则让出CPU
            Thread.yield();
        }
    }

    //解锁
    public void unlock()
    {
        //设置锁定为false
        _locked.set(false);
    }
}