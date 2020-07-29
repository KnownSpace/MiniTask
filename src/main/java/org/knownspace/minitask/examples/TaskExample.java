package org.knownspace.minitask.examples;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.knownspace.minitask.ITask;
import org.knownspace.minitask.ITaskFactory;
import org.knownspace.minitask.TaskFactory;

public class TaskExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ITaskFactory startup = new TaskFactory(executor);
        //start task
        ITask<Void> t1 = startup.startTask(()->{
            System.out.println("Hello World");
        });
        //then
        //return value
        ITask<Void> t2 = t1.then(()->{
            System.out.println("Return Value 1");
            return 1;
        })
        //then
        //throw exception
        .then((Integer i)->{
            System.out.printf("Last Return Value is %d\n",i);
            throw new Exception("Error");
        })
        //then with exception
        .thenWithException((future)->{
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //wait last task complete
        try {
            t2.waitUntilComplete();
        } catch (Exception ignore) {}
        executor.shutdown();
    }
}