package com.jay.bio;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName TimeServerHandlerExecutePool
 * @Description Time服务器处理类的线程池，当接受到新的客户端连接时，将请求Socket分装成一个Task，
 *              然后调用线程池的execute方法执行，从而避免了每个请求接入时都创建一个新的线程
 * @author lufangjie
 * @Version 1.0
 **/
public class TimeServerHandlerExecutePool {

    private ExecutorService executor;

    private static final int DEFAULT_MAX_POOL_SIZE = 50;
    private static final int DEFAULT_QUEUE_SIZE = 10000;

    TimeServerHandlerExecutePool() {    // package-private
        this(DEFAULT_MAX_POOL_SIZE, DEFAULT_QUEUE_SIZE);
    }

    TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {  // package-private
        executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                maxPoolSize,
                120L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new MyThreadFactory());
    }

    void execute(Runnable task) {
        executor.execute(task);
    }

    static class MyThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private String threadName;

        MyThreadFactory() {
            threadName = "time-server-" + poolNumber.getAndIncrement() + "-pool-%d";
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, threadName);
        }
    }
}
