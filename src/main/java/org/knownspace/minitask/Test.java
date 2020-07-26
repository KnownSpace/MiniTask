package org.knownspace.minitask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.knownspace.minitask.locks.ReadWriteFlag;
import org.knownspace.minitask.locks.SharedFlag;
import org.knownspace.minitask.locks.UniqueFlag;

public class Test {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ITaskFactory factory = new TaskFactory(executorService);

        //互斥体
        UniqueFlag uniqueFlag = factory.makeUniqueFlag();
        //只出现以下中的一个
        uniqueFlag.lock().then(()->{
            //故意不释放
            //uniqueFlag.unlock();
            System.out.println("Unique Lock 1");
        });
        uniqueFlag.lock().then(()->{
            System.out.println("Unique Lock 2");
        });

        //信号量
        //同时允许三个线程访问
        SharedFlag sharedFlag = factory.makeSharedFlag(3);
        //只出现以下中的三个
        sharedFlag.lock().then(()->{
            //故意不释放
            //sharedFlag.unlock();
            System.out.println("Shared Lock 1");
        });
        sharedFlag.lock().then(()->{
            //故意不释放
            //sharedFlag.unlock();
            System.out.println("Shared Lock 2");
        });
        sharedFlag.lock().then(()->{
            //故意不释放
            //sharedFlag.unlock();
            System.out.println("Shared Lock 3");
        });
        sharedFlag.lock().then(()->{
            //故意不释放
            //sharedFlag.unlock();
            System.out.println("Shared Lock 4");
        });

        //读写语义锁
        //允许并行地读或互斥地写
        ReadWriteFlag readWriteFlag = factory.makeReadWriteFlag();
        //以随机顺序出现
        readWriteFlag.lockRead().then(()->{
            System.out.println("Read 1");
            readWriteFlag.unlock();
        });
        readWriteFlag.lockRead().then(()->{
            System.out.println("Read 2");
            readWriteFlag.unlock();
        });
        readWriteFlag.lockWrite().then(()->{
            System.out.println("Write 1");
            //故意不释放
            //readWriteFlag.unlock();
        }).then(()->{
            //绝不出现
            readWriteFlag.lockRead().then(()->{
                System.out.println("Read 3");
            });
        });
        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {}
        executorService.shutdown();
    }
}