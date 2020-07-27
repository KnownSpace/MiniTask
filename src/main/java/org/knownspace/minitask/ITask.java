package org.knownspace.minitask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.knownspace.minitask.functions.Callable;
import org.knownspace.minitask.functions.Callback;
import org.knownspace.minitask.functions.Consumer;
import org.knownspace.minitask.functions.Provider;

public interface ITask<Result> extends Runnable {
    public ExecutorService getExecutor();

    public ITask<Void> then(Consumer<Result> next);

    public <NewResult> ITask<NewResult> then(Callable<NewResult,Result> next);

    public ITask<Void> then(Callback runnable);

    public  <NewResult> ITask<NewResult> then(Provider<NewResult> next);

    public ITask<Void> thenWithException(Consumer<Future<Result>> next);

    public <NewResult> ITask<NewResult> thenWithException(Callable<NewResult,Future<Result>> next);

    public Result get() throws InterruptedException,ExecutionException;

    public void waitUntilComplete() throws InterruptedException;
}