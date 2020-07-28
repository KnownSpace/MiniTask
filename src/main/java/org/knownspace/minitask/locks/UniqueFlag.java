package org.knownspace.minitask.locks;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.knownspace.minitask.Helper;
import org.knownspace.minitask.ITask;
import org.knownspace.minitask.ITaskCompletionEvent;
import org.knownspace.minitask.TaskCompletionEvent;

public class UniqueFlag implements IUnlockable {

    //自选锁
    //用于保护内部数据
    private SpinLock _lock;

    //等待队列
    //元素类型是完成事件
    private List<ITaskCompletionEvent<Void>> _waits;

    //指示锁是否被占用
    private boolean _locked;

    //在哪一个线程池执行通知操作
    private ExecutorService _executor;

    //构造器
    public UniqueFlag(ExecutorService executor)
    {
        //初始化数据
        _lock = new SpinLock();
        _waits = new LinkedList<>();
        _locked = false;
        _executor = executor;
    }

    //加锁
    public ITask<Void> lock()
    {
        //构造完成事件
        ITaskCompletionEvent<Void> ce = new TaskCompletionEvent<>(_executor);
        //对内部数据加锁
        _lock.lock();
        try (Unlocker<SpinLock> raiiUnlocker = new Unlocker<SpinLock>(_lock)) {
            //如果未被锁定
            if(!_locked) {
                //设置为锁定
                _locked = true;
                //提前解锁
                raiiUnlocker.unlock();
                //进行通知
                ce.complete(Helper.voidValue);
            } else {
                //已被锁定
                //加入等待队列
                _waits.add(ce);
            }
        } catch (Exception ignore) {}
        //返回完成事件所绑定的Task
        return ce.getTask();
    }

    //解锁
    @Override
    public void unlock() {
        //对内部数据加锁
        _lock.lock();
        try (Unlocker<SpinLock> raiiUnlocker = new Unlocker<SpinLock>(_lock)) {
            //如果等待队列为空
            if(_waits.isEmpty()) {
                //设置为未锁定
                _locked = false;
            } else {
                //获取第一个完成事件
                ITaskCompletionEvent<Void> ce = _waits.get(0);
                //从队列中弹出
                _waits.remove(0);
                //提前解锁
                raiiUnlocker.unlock();
                //进行通知
                ce.complete(Helper.voidValue);
            }
        } catch (Exception ignore) {}
    }
}