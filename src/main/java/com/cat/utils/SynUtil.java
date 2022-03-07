package com.cat.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author CAT
 */
public class SynUtil {
    private SynUtil() {

    }

    public static final BlockingQueue<Integer> START_SIGNAL_QUEUE = new SynchronousQueue<>();
    public static final BlockingQueue<Integer> STOP_SIGNAL_QUEUE = new SynchronousQueue<>();
    public static final AtomicBoolean WORK_THREAD_RUNNING = new AtomicBoolean(false);
}
