package org.knownspace.minitask;

import java.util.concurrent.ExecutorService;

import org.knownspace.minitask.functions.Callback;
import org.knownspace.minitask.functions.Provider;
import org.knownspace.minitask.locks.ReadWriteFlag;
import org.knownspace.minitask.locks.SharedFlag;
import org.knownspace.minitask.locks.UniqueFlag;

public class TaskFactory implements ITaskFactory {

    private ExecutorService _executor;

    public TaskFactory(ExecutorService executor) {
        _executor = executor;
    }

    @Override
    public UniqueFlag makeUniqueFlag() {
        return new UniqueFlag(_executor);
    }

    @Override
    public <Result> ITask<Result> startTask(Provider<Result> fn) {
        ITask<Result> task = new Task<>(_executor, fn);
        task.run();
        return task;
    }

    @Override
    public ITask<Void> startTask(Callback fn) {
        ITask<Void> task = new Task<>(_executor, fn);
        task.run();
        return task;
    }

    @Override
    public <Result> ITaskCompletionEvent<Result> makeCompletionEvent() {
        return new TaskCompletionEvent<Result>(_executor);
    }

    @Override
    public SharedFlag makeSharedFlag(int count) {
        return new SharedFlag(count,_executor);
    }

    @Override
    public ReadWriteFlag makeReadWriteFlag() {
        return new ReadWriteFlag(_executor);
    }

    @Override
    public ITask<Void> complete() {
        ITaskCompletionEvent<Void> ce = new TaskCompletionEvent<>(_executor);
        ce.complete(TaskHelper.voidValue);
        return ce.getTask();
    }

    @Override
    public <Result> ITask<Result> complete(Result result) {
        ITaskCompletionEvent<Result> ce = new TaskCompletionEvent<>(_executor);
        ce.complete(result);
        return ce.getTask();
    }

    @Override
    public <Result> ITask<Result> fromException(Exception exception) {
        ITaskCompletionEvent<Result> ce = new TaskCompletionEvent<>(_executor);
        ce.complete(exception);
        return ce.getTask();
    }
}