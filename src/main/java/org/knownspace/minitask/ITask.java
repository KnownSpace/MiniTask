package org.knownspace.minitask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ITask<Result> extends Runnable {
    public ExecutorService getExecutor();

    public ITask<Void> then(Consumer<Result> next);

    public <NewResult> ITask<NewResult> then(Function<Result,NewResult> next);

    public ITask<Void> then(Runnable runnable);

    public  <NewResult> ITask<NewResult> then(Supplier<NewResult> next);

    public ITask<Void> thenWithException(Consumer<Future<Result>> next);

    public <NewResult> ITask<NewResult> thenWithException(Function<Future<Result>,NewResult> next);

    public Result get() throws InterruptedException,ExecutionException;

    public void waitUntilComplete() throws InterruptedException;
}