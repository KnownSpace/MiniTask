package org.knownspace.minitask;

public interface ITaskCompletionEvent<Result> {
    public ITask<Result> getTask();

    public void complete(Result value);

    public void complete(Throwable throwable);
}