package org.knownspace.minitask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ITaskFactory factory = new TaskFactory(executorService);
        ITask<Void> task = factory.startTask(()->{
            System.out.println("Hello World");
        });
        task.then(()->{
            System.out.println("Next");
            return 1;
        }).then((Integer i)->{
            System.out.println(i);
            if(i == 1) {
                return i;
            } else {
                throw new Exception("Error");
            }
        }).thenWithException((f)->{
            try {
                Integer i = f.get();
                System.out.println(i);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });
        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {}
        executorService.shutdown();
    }
}