# MiniTask

A simple Java task library

[![JIT Pack](https://www.jitpack.io/v/KnownSpace/MiniTask.svg)](https://www.jitpack.io/#KnownSpace/MiniTask)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/babc84731815428eb9c25f6ff6363b17)](https://www.codacy.com/gh/KnownSpace/MiniTask?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=KnownSpace/MiniTask&amp;utm_campaign=Badge_Grade)

## Build

```bash
git clone https://github.com/KnownSpace/MiniTask
cd MiniTask
gradle build
```

## Contribute

1. Fork MiniTask

2. Write your code

3. Open a pull request

## Usage

### Task

```java
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
```

---

### Locks

```java
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
```
