package org.knownspace.minitask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class TaskCompletionEvent<Result> implements ITaskCompletionEvent<Result> {

    private CompletableFuture<Result> _future;

    private Task<Result> _task;

    public TaskCompletionEvent(ExecutorService executor)
    {
        _future = new CompletableFuture<>();
        _task = Task.fromFuture(executor,_future);
    }

    @Override
    public ITask<Result> getTask() {
        return _task;
    }

    @Override
    public void complete(Result value) {
        _future.complete(value);
        _task.run();
    }

    @Override
    public void complete(Throwable throwable) {
        _future.completeExceptionally(throwable);
        _task.run();
    }
    
}