package org.knownspace.minitask.examples;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.knownspace.minitask.ITaskFactory;
import org.knownspace.minitask.TaskFactory;
import org.knownspace.minitask.locks.ReadWriteFlag;
import org.knownspace.minitask.locks.SharedFlag;
import org.knownspace.minitask.locks.UniqueFlag;
import org.knownspace.minitask.locks.Unlocker;

public class LockExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ITaskFactory startup = new TaskFactory(executor);
        //unique lock
        UniqueFlag uniqueFlag = startup.makeUniqueFlag();
        //shared lock
        SharedFlag sharedFlag = startup.makeSharedFlag(3);
        //read write lock
        ReadWriteFlag readWriteFlag = startup.makeReadWriteFlag();
        final int testCount = 20;
        List<Integer> list = new LinkedList<>();
        for(int i = 0;i < testCount;++i) {
            final int val = i;
            //push
            startup.startTask(()->{
                uniqueFlag.lock()
                .then(()->{
                    try (Unlocker<UniqueFlag> unlocker = new Unlocker<>(uniqueFlag)) {
                        list.add(val);
                    } catch (Exception ignore) {}
                });
            });
        }
        //wait while preparing data
        while(list.size() != testCount) {
            Thread.yield();
        }
        //foreach
        for(int i = 0;i < testCount;++i) {
            final int index = i;
            startup.startTask(()->{
                sharedFlag.lock().then(()->{
                    try (Unlocker<SharedFlag> unlocker = new Unlocker<>(sharedFlag)) {
                        System.out.printf("List[%d] is %d\n",index,list.get(index));
                    } catch (Exception e) {}
                });
            });
        }

        try {
            Thread.sleep(5000);
        } catch (Exception ignore) {}

        list.clear();
        for(int i =0; i<testCount; ++i) {
            final int val = i;
            readWriteFlag.lockWrite().then(()->{
                try (Unlocker<ReadWriteFlag> unlocker = new Unlocker<ReadWriteFlag>(readWriteFlag)) {
                    list.add(val);
                } catch (Exception ignore) {}
            });
        }
        //wait while preparing data
        while(list.size() != testCount) {
            Thread.yield();
        }
        for(int i =0; i<testCount; ++i) {
            final int index = i;
            readWriteFlag.lockRead().then(()->{
                try (Unlocker<ReadWriteFlag> unlocker = new Unlocker<ReadWriteFlag>(readWriteFlag)) {
                    System.out.printf("RW List[%d] is %d\n",index,list.get(index));
                } catch (Exception ignore) {}
            });
        }
        try {
            Thread.sleep(5000);
        } catch (Exception ignore) {}
        executor.shutdown();
    }
}