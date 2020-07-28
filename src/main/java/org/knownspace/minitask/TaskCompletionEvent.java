package org.knownspace.minitask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

//完成事件
public class TaskCompletionEvent<Result> implements ITaskCompletionEvent<Result> {

    //Future
    private CompletableFuture<Result> _future;

    //完成事件所绑定的Task
    private Task<Result> _task;

    //构造器
    public TaskCompletionEvent(ExecutorService executor)
    {
        //初始化成员
        _future = new CompletableFuture<>();
        //将_task绑定到_future上
        _task = Task.fromFuture(executor,_future);
    }

    //返回完成事件所绑定的Task
    @Override
    public ITask<Result> getTask() {
        return _task;
    }

    //通知事件已完成
    @Override
    public void complete(Result value) {
        //设置future
        _future.complete(value);
        //将task放入线程池
        _task.run();
    }

    //已异常的形式完成
    @Override
    public void complete(Throwable throwable) {
        //设置future
        _future.completeExceptionally(throwable);
        //将task放入线程池
        _task.run();
    }
}