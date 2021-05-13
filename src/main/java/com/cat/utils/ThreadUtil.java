package com.cat.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author CAT
 */
public class ThreadUtil {
    private ThreadUtil() {

    }

    private static final BlockingQueue<Integer> START_CONTROL_MESSAGE_QUEUE = new SynchronousQueue<>();
    private static final BlockingQueue<Integer> STOP_CONTROL_MESSAGE_QUEUE = new SynchronousQueue<>();
    public static final AtomicBoolean WORK_THREAD_RUNNING = new AtomicBoolean(false);

    public static BlockingQueue<Integer> getStartControlMessageQueue() {
        return START_CONTROL_MESSAGE_QUEUE;
    }

    public static BlockingQueue<Integer> getStopControlMessageQueue() {
        return STOP_CONTROL_MESSAGE_QUEUE;
    }
}
