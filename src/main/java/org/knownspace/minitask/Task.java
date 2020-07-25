package org.knownspace.minitask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import java.util.function.Function;

public class Task<Result> implements ITask<Result> {

    private ExecutorService _executor;

    private Supplier<Result> _fn;

    private CompletableFuture<Result> _future;

    public Task(ExecutorService executor, Supplier<Result> fn) {
        this(executor, fn, new CompletableFuture<>());
    }

    public Task(ExecutorService executor,Runnable fn){
        this(executor,()->{ 
            fn.run();
            return null;},new CompletableFuture<>());
    }

    private Task(ExecutorService executor, Supplier<Result> fn, CompletableFuture<Result> future) {
        _executor = executor;
        _fn = fn;
        _future = future;
    }

    // never call run by yourself
    @Override
    public void run() {
        if(_fn == null)
        {
            return;
        }
        _executor.execute(() -> {
            try {
                Result result = _fn.get();
                _future.complete(result);
            } catch (Exception e) {
                _future.completeExceptionally(e);
            }
        });
    }

    @Override
    public ExecutorService getExecutor() {
        return _executor;
    }

    @Override
    public ITask<Void> then(Consumer<Result> next) {
        CompletableFuture<Void> future = _future.thenAcceptAsync(next,_executor);
        ITask<Void> task = new Task<Void>(_executor,null,future);
        return task;
    }

    @Override
    public <NewResult> ITask<NewResult> then(Function<Result, NewResult> next) {
        CompletableFuture<NewResult> future = _future.thenApplyAsync(next,_executor);
        ITask<NewResult> task = new Task<NewResult>(_executor,null,future);
        return task;
    }

    @Override
    public ITask<Void> then(Runnable runnable) {
       CompletableFuture<Void> future = _future.thenRunAsync(runnable,_executor);
       ITask<Void> task = new Task<Void>(_executor,null,future);
        return task;
    }

    @Override
    public ITask<Void> thenWithException(Consumer<Future<Result>> next) {
        CompletableFuture<Result> result_future = new CompletableFuture<>();
        ITask<Void> task = new Task<>(_executor,()->{
            next.accept(result_future);
            return null;
        });
        Consumer<Result> consumer = (r)->
        {
            result_future.complete(r);
            task.run();
        };
        _future.thenAcceptAsync(consumer,_executor);
        _future.exceptionally((ex)->{
            result_future.completeExceptionally(ex);
            task.run();
            return null;
        });
        return task;
    }

    @Override
    public <NewResult> ITask<NewResult> thenWithException(Function<Future<Result>, NewResult> next) {
        CompletableFuture<Result> result_future = new CompletableFuture<>();
        ITask<NewResult> task = new Task<>(_executor,()->{
            
            return next.apply(result_future);
        });
        Consumer<Result> consumer = (r)->
        {
            result_future.complete(r);
            task.run();
        };
        _future.thenAcceptAsync(consumer,_executor);
        _future.exceptionally((ex)->{
            result_future.completeExceptionally(ex);
            task.run();
            return null;
        });
        return task;
    }

    @Override
    public Result get() throws InterruptedException,ExecutionException {
        return _future.get();
    }

    @Override
    public void waitUntilComplete() throws InterruptedException {
        try {
            get();
        } catch (InterruptedException e) {
           throw e;
        } catch(ExecutionException ignore) {
        }
    }

    public static<R> Task<R> fromFuture(ExecutorService executor,CompletableFuture<R> future) {
        Task<R> task = new Task<>(executor,null,future);
        return task;
    }

    @Override
    public <NewResult> ITask<NewResult> then(Supplier<NewResult> next) {
        CompletableFuture<NewResult> future = _future.thenApplyAsync((ignore)->{
            return next.get();
        },_executor);
        ITask<NewResult> task = new Task<NewResult>(_executor,null,future);
        return task;
    }
}