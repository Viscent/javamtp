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

package io.github.viscent.mtpattern.ch5.tpt.example;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;
import io.github.viscent.util.Tools;

public class SomeService {
    private final BlockingQueue<String> queue =
            new ArrayBlockingQueue<String>(
                    100);

    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();

    private class Producer extends AbstractTerminatableThread {
        private int i = 0;

        @Override
        protected void doRun() throws Exception {
            queue.put(String.valueOf(i++));
            consumer.terminationToken.reservations.incrementAndGet();
        }

    };

    private class Consumer extends AbstractTerminatableThread {

        @Override
        protected void doRun() throws Exception {
            String product = queue.take();
            System.out.println("Processing product:" + product);

            // 模拟执行真正操作的时间消耗
            try {
                Tools.randomPause(100, 50);
            } finally {
                terminationToken.reservations.decrementAndGet();
            }

        }

    }

    public void shutdown() {
        // 生产者线程停止后再停止消费者线程
        producer.terminate(true);
        consumer.terminate();
    }

    public void init() {
        producer.start();
        consumer.start();
    }

    public static void main(String[] args) throws InterruptedException {
        SomeService ss = new SomeService();
        ss.init();
        Thread.sleep(500);
        ss.shutdown();
    }
}
