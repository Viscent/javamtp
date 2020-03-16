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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

public class CustomScheduler implements Runnable {
    final LinkedBlockingQueue<Runnable> activationQueue =
            new LinkedBlockingQueue<Runnable>();

    @Override
    public void run() {
        dispatch();
    }

    public <T> Future<T> enqueue(Callable<T> methodRequest) {
        final FutureTask<T> task = new FutureTask<T>(methodRequest) {

            @Override
            public void run() {
                try {
                    super.run();
                    // 捕获所以可能抛出的对象，避免该任务运行失败而导致其所在的线程终止。
                } catch (Throwable t) {
                    this.setException(t);
                }
            }

        };

        try {
            activationQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return task;
    }

    public void dispatch() {
        while (true) {
            Runnable methodRequest;
            try {
                methodRequest = activationQueue.take();

                // 防止个别任务执行失败导致线程终止的代码在run方法中
                methodRequest.run();
            } catch (InterruptedException e) {
                // 处理该异常
            }

        }
    }

    public static void main(String[] args) {

        CustomScheduler scheduler = new CustomScheduler();
        Thread t = new Thread(scheduler);
        t.start();
        Future<String> result = scheduler.enqueue(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Thread.sleep(1500);
                int i = 1;
                if (1 == i) {
                    throw new RuntimeException("test");
                }
                return "ok";
            }

        });

        try {
            System.out.println(result.get());
            ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
}
