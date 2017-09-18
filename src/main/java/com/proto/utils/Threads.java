package com.proto.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Useful stuff for working with threads.
 *
 * @author wfrancis
 */
public final class Threads {
    /**
     * Cause the current thread to sleep for up to <code>millis</code> 
     * milliseconds. If the thread is interrupted, this method will return 
     * immediately.
     * 
     * @param millis  the desired sleep interval, in milliseconds.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }
    }

    /**
     * Create a new {@link ThreadFactory} that names threads.
     * 
     * @param name      the name to assign to threads. Threads will be named
     *                  <code>"<i>name</i>-<i>id"</i></code>, where 
     *                  <code>id</code> is a zero-based counter.
     *                  
     * @return the factory.
     */
    public static ThreadFactory createFactory(final String name) {
        return createFactory(name, false);
    }
    
    /**
     * Create a new {@link ThreadFactory} that names threads and optionally sets
     * the daemon flag.
     * 
     * @param name      the name to assign to threads. Threads will be named
     *                  <code>"<i>name</i>-<i>id"</i></code>, where 
     *                  <code>id</code> is a zero-based counter.
     * @param isDaemon  if <code>true</code>, the created threads will be daemon
     *                  threads.
     *                  
     * @return the factory.
     */
    public static ThreadFactory createFactory(final String name, final boolean isDaemon) {
        return new ThreadFactory() {
            /*
             * counter
             */
            private final AtomicInteger _counter = new AtomicInteger(0);
            
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, name + "-" + _counter.incrementAndGet());
                if (isDaemon) {
                    t.setDaemon(true);
                }
                return t;
            }        
        };
    }
    
    /**
     * Not instantiated.
     */
    private Threads() {
        //
    }

    /**
     * A empty runnable
     */
    public static Runnable NO_RUN = new Runnable() {
        public void run() {}
    };
}
