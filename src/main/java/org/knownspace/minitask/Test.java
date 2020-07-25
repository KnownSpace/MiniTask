package org.knownspace.minitask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ITaskFactory factory = new TaskFactory(executorService);
        ITask<Void> task = factory.startTask(()->{
            System.out.println("Hello World!");
        });
        ITask<Integer> task2 = task.then(()->{
            return 1;
        });
        try {
            task2.waitUntilComplete();
        } catch (Exception ignore) {
        }
        executorService.shutdown();
    }
}