package com.cat.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author CAT
 */
public class Threads {
    private Threads() {

    }

    /**
     * 同步锁对象
     */
    public static final Object LOCK = new Object();
    /**
     * 同步等待时间
     */
    public static final long WAIT_TIME = 3_000L;

    public static final ExecutorService EMAIL_POOL = getPresetExecutorService("EmailPool");

    /**
     * 获取一个预设的线程池对象。
     *
     * @param poolName 线程池名称
     * @return 线程池
     */
    public static ExecutorService getPresetExecutorService(String poolName) {
        return new ThreadPoolExecutor(4, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> new Thread(r, poolName + "-Thread-" + r.hashCode()));
    }
}
