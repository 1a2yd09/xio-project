package com.cat.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author CAT
 */
public class ThreadPoolFactory {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE_SIZE = 1;
    private static final int KEEP_ALIVE_TIME = 0;
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private static final ExecutorService SERVICE_THREAD_POOL;

    static {
        ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1);
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        ThreadFactory threadFactory = r -> new Thread(r, "service-pool-thread-" + THREAD_COUNTER.incrementAndGet());
        SERVICE_THREAD_POOL = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS,
                workQueue,
                threadFactory,
                handler);
    }

    private ThreadPoolFactory() {
    }

    public static ExecutorService getServiceThreadPool() {
        return SERVICE_THREAD_POOL;
    }
}
