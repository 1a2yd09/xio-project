package com.cat.utils;

import com.cat.pojo.CuttingSignal;

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
    private static final BlockingQueue<CuttingSignal> CUTTING_MESSAGE_QUEUE = new SynchronousQueue<>();
    private static final BlockingQueue<String> ACTION_PROCESSED_MESSAGE_QUEUE = new SynchronousQueue<>();
    public static final AtomicBoolean WORK_THREAD_RUNNING = new AtomicBoolean(false);

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
