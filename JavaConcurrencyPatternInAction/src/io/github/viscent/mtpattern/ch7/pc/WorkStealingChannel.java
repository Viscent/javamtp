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

package io.github.viscent.mtpattern.ch7.pc;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

public class WorkStealingChannel<T> implements WorkStealingEnabledChannel<T> {
    // 受管队列
    private final BlockingDeque<T>[] managedQueues;

    public WorkStealingChannel(BlockingDeque<T>[] managedQueues) {
        this.managedQueues = managedQueues;
    }

    @Override
    public T take(BlockingDeque<T> preferredQueue) throws InterruptedException {

        // 优先从指定的受管队列中取“产品”
        BlockingDeque<T> targetQueue = preferredQueue;
        T product = null;

        // 试图从指定的队列队首取“产品”
        if (null != targetQueue) {
            product = targetQueue.poll();
        }

        int queueIndex = -1;

        while (null == product) {
            queueIndex = (queueIndex + 1) % managedQueues.length;
            targetQueue = managedQueues[queueIndex];
            // 试图从其它受管队列的队尾“窃取”“产品”
            product = targetQueue.pollLast();
            if (preferredQueue == targetQueue) {
                break;
            }
        }

        if (null == product) {

            // 随机”窃取“其它受管队列的”产品“
            queueIndex = (int) (System.currentTimeMillis()
                    % managedQueues.length);
            targetQueue = managedQueues[queueIndex];
            product = targetQueue.takeLast();
            System.out.println("stealed from " + queueIndex + ":" + product);
        }

        return product;
    }

    @Override
    public void put(T product) throws InterruptedException {
        int targetIndex = (product.hashCode() % managedQueues.length);
        BlockingQueue<T> targetQueue = managedQueues[targetIndex];
        targetQueue.put(product);
    }

    @Override
    public T take() throws InterruptedException {
        return take(null);
    }

}
