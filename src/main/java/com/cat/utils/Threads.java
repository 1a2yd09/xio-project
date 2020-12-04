package com.cat.utils;

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
}
