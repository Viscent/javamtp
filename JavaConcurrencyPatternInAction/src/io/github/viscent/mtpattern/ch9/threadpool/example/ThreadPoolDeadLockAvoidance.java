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

package io.github.viscent.mtpattern.ch9.threadpool.example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.viscent.util.Debug;

public class ThreadPoolDeadLockAvoidance {
    private final ThreadPoolExecutor threadPool =
            new ThreadPoolExecutor(1,
                    // 最大线程池大小为1（有限数值）：
                    1, 60, TimeUnit.SECONDS,
                    // 工作队列为SynchronousQueue：
                    new SynchronousQueue<Runnable>(),
                    // 线程池饱和处理策略为CallerRunsPolicy：
                    new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) {
        ThreadPoolDeadLockAvoidance me = new ThreadPoolDeadLockAvoidance();
        me.test("<This will NOT deadlock>");
    }

    public void test(final String message) {
        Runnable taskA = new Runnable() {

            @Override
            public void run() {
                Debug.info("Executing TaskA...");
                Runnable taskB = new Runnable() {
                    @Override
                    public void run() {
                        Debug.info("TaskB processes " + message);
                    }
                };
                Future<?> result = threadPool.submit(taskB);

                try {
                    // 等待TaskB执行结束才能继续执行TaskA，使TaskA和TaskB称为由依赖关系的两个任务
                    result.get();
                } catch (InterruptedException e) {
                    ;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                Debug.info("TaskA Done.");
            }
        };

        threadPool.submit(taskA);
    }

}
