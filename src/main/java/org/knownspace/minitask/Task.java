package org.knownspace.minitask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.knownspace.minitask.functions.Callable;
import org.knownspace.minitask.functions.Callback;
import org.knownspace.minitask.functions.Provider;
import org.knownspace.minitask.functions.Consumer;

public class Task<Result> implements ITask<Result> {

    private ExecutorService _executor;

    private Provider<Result> _fn;

    private CompletableFuture<Result> _future;

    public Task(ExecutorService executor, Provider<Result> fn) {
        this(executor, fn, new CompletableFuture<>());
    }

    public Task(ExecutorService executor,Callback fn){
        this(executor,()->{ 
            fn.call();
            return null;},new CompletableFuture<>());
    }

    private Task(ExecutorService executor, Provider<Result> fn, CompletableFuture<Result> future) {
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
                Result result = _fn.provide();
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
        CompletableFuture<Void> future = new CompletableFuture<>();
        _future.thenAcceptAsync((arg)->{
            try {
                next.call(arg);
                future.complete(TaskHelper.voidValue);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        },_executor);
        ITask<Void> task = new Task<Void>(_executor,null,future);
        return task;
    }

    @Override
    public <NewResult> ITask<NewResult> then(Callable<NewResult,Result> next) {
        CompletableFuture<NewResult> future = new CompletableFuture<>();
        _future.thenAcceptAsync((arg)->{
            try {
                NewResult r = next.call(arg);
                future.complete(r);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        },_executor);
        ITask<NewResult> task = new Task<NewResult>(_executor,null,future);
        return task;
    }

    @Override
    public ITask<Void> then(Callback runnable) {
       CompletableFuture<Void> future =  new CompletableFuture<>();
       _future.thenRunAsync(()->{
           try {
               runnable.call();
               future.complete(TaskHelper.voidValue);
           } catch (Exception e) {
               future.completeExceptionally(e);
           }
       },_executor);
       ITask<Void> task = new Task<Void>(_executor,null,future);
        return task;
    }

    @Override
    public ITask<Void> thenWithException(Consumer<Future<Result>> next) {
        CompletableFuture<Result> result_future = new CompletableFuture<>();
        ITask<Void> task = new Task<>(_executor,()->{
            next.call(result_future);
            return null;
        });
        Consumer<Result> consumer = (r)->
        {
            result_future.complete(r);
            task.run();
        };
        _future.thenAcceptAsync((arg)->{
            try {
                consumer.call(arg);
            } catch (Exception ignore) {}
        },_executor);
        _future.exceptionally((ex)->{
            result_future.completeExceptionally(ex);
            task.run();
            return null;
        });
        return task;
    }

    @Override
    public <NewResult> ITask<NewResult> thenWithException(Callable<NewResult,Future<Result>> next) {
        CompletableFuture<Result> result_future = new CompletableFuture<>();
        ITask<NewResult> task = new Task<>(_executor,()->{
            return next.call(result_future);
        });
        Consumer<Result> consumer = (r)->
        {
            result_future.complete(r);
            task.run();
        };
        _future.thenAcceptAsync((arg)->{
            try {
                consumer.call(arg);
            } catch (Exception ignore) {}
        },_executor);
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
    public <NewResult> ITask<NewResult> then(Provider<NewResult> next) {
        CompletableFuture<NewResult> future = new CompletableFuture<>();
        _future.thenAcceptAsync((ignore)->{
            try {
                NewResult r = next.provide();
                future.complete(r);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        },_executor);
        ITask<NewResult> task = new Task<NewResult>(_executor,null,future);
        return task;
    }
}