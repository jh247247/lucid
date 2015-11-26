package com.lightspeed.gpr.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public class ThreadPoolHandler {
    static ListeningExecutorService m_executor =
        MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(4,
                                                                      new LowPriorityThreadFactory()));

    //static ListeningExecutorService m_executor =
    //  MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    static class LowPriorityThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread ret = new Thread(r);
            ret.setPriority(Thread.MIN_PRIORITY);
            return ret;
        }
    }

    public static <T> ListenableFuture<T> submit(Callable<T> c) {
        return m_executor.submit(c);
    }
}
