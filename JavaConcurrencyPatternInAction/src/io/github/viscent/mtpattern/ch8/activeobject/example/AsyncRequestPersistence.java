/*
授权声明：
本源码系《Java多线程编程实战指南（设计模式篇）第2版》一书（ISBN：978-7-121-38245-1，以下称之为“原书”）的配套源码，
欲了解本代码的更多细节，请参考原书。
本代码仅为原书的配套说明之用，并不附带任何承诺（如质量保证和收益）。
以任何形式将本代码之部分或者全部用于营利性用途需经版权人书面同意。
将本代码之部分或者全部用于非营利性用途需要在代码中保留本声明。
任何对本代码的修改需在代码中以注释的形式注明修改人、修改时间以及修改内容。
本代码可以从以下网址下载：
https://github.com/Viscent/javamtp
http://www.broadview.com.cn/38245
*/

package io.github.viscent.mtpattern.ch8.activeobject.example;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

//模式角色：ActiveObject.Proxy
public class AsyncRequestPersistence implements RequestPersistence {
    private static final long ONE_MINUTE_IN_SECONDS = 60;
    final static Logger logger =
            Logger.getLogger(AsyncRequestPersistence.class);
    final AtomicLong taskTimeConsumedPerInterval = new AtomicLong(0);
    final AtomicInteger requestSubmittedPerInterval = new AtomicInteger(0);

    // 模式角色：ActiveObject.Servant
    private final DiskbasedRequestPersistence delegate =
            new DiskbasedRequestPersistence();

    // 模式角色：ActiveObject.Scheduler
    private final ThreadPoolExecutor scheduler;

    // 用于保存AsyncRequestPersistence的唯一实例
    private static class InstanceHolder {
        final static RequestPersistence INSTANCE =
                new AsyncRequestPersistence();
    }

    // 私有构造器
    private AsyncRequestPersistence() {
        scheduler = new ThreadPoolExecutor(1, 3, 60 * ONE_MINUTE_IN_SECONDS,
                TimeUnit.SECONDS,
                // 模式角色：ActiveObject.ActivationQueue
                new ArrayBlockingQueue<Runnable>(200), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t;
                        t = new Thread(r, "AsyncRequestPersistence");
                        return t;
                    }

                });

        scheduler.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy());

        // 启动队列监控定时任务
        Timer monitorTimer = new Timer(true);
        monitorTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (logger.isInfoEnabled()) {
                    logger.info("task count:" + requestSubmittedPerInterval
                            + ",Queue size:" + scheduler.getQueue().size()
                            + ",taskTimeConsumedPerInterval:"
                            + taskTimeConsumedPerInterval.get() + " ms");
                }

                taskTimeConsumedPerInterval.set(0);
                requestSubmittedPerInterval.set(0);
            }
        }, 0, ONE_MINUTE_IN_SECONDS * 1000);

    }

    // 获取类AsyncRequestPersistence的唯一实例
    public static RequestPersistence getInstance() {
        return InstanceHolder.INSTANCE;
    }

    // 类MMSDeliverRequest的源码参见本书的配套下载。
    @Override
    public void store(final MMSDeliverRequest request) {
        /*
         * 将对store方法的调用封装成MethodRequest对象, 并存入缓冲区。
         */
        // 模式角色：ActiveObject.MethodRequest
        Callable<Boolean> methodRequest = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis();
                try {
                    delegate.store(request);
                } finally {
                    taskTimeConsumedPerInterval
                            .addAndGet(System.currentTimeMillis() - start);
                }
                return Boolean.TRUE;
            }
        };// end of Callable
        scheduler.submit(methodRequest);

        requestSubmittedPerInterval.incrementAndGet();
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdown();
    }

}