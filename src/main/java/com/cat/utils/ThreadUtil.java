package com.cat.utils;

import com.cat.pojo.CuttingSignal;

import java.util.concurrent.*;

/**
 * @author CAT
 */
public class ThreadUtil {
    private ThreadUtil() {

    }

    private static final BlockingQueue<Integer> START_CONTROL_MESSAGE_QUEUE = new SynchronousQueue<>();
    private static final BlockingQueue<Integer> STOP_CONTROL_MESSAGE_QUEUE = new SynchronousQueue<>();
    private static final BlockingQueue<CuttingSignal> CUTTING_MESSAGE_QUEUE = new SynchronousQueue<>();
    private static final BlockingQueue<String> ACTION_PROCESSED_MESSAGE_QUEUE = new SynchronousQueue<>();

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

    public static BlockingQueue<Integer> getStartControlMessageQueue() {
        return START_CONTROL_MESSAGE_QUEUE;
    }

    public static BlockingQueue<Integer> getStopControlMessageQueue() {
        return STOP_CONTROL_MESSAGE_QUEUE;
    }

    public static BlockingQueue<CuttingSignal> getCuttingMessageQueue() {
        return CUTTING_MESSAGE_QUEUE;
    }

    public static BlockingQueue<String> getActionProcessedMessageQueue() {
        return ACTION_PROCESSED_MESSAGE_QUEUE;
    }
}
