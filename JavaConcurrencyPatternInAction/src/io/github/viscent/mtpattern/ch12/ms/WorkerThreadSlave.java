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

package io.github.viscent.mtpattern.ch12.ms;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;

/**
 * 基于工作者线程的Slave参与者通用实现。
 * 
 * @author Viscent Huang
 *
 * @param <T>
 *            子任务类型
 * @param <V>
 *            子任务处理结果类型
 */
public abstract class WorkerThreadSlave<T, V> extends
        AbstractTerminatableThread implements SlaveSpec<T, V> {
    private final BlockingQueue<Runnable> taskQueue;

    public WorkerThreadSlave(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    public Future<V> submit(final T task) throws InterruptedException {

        FutureTask<V> ft = new FutureTask<V>(new Callable<V>() {

            @Override
            public V call() throws Exception {
                V result;

                try {
                    result = doProcess(task);
                } catch (Exception e) {
                    SubTaskFailureException stfe =
                            newSubTaskFailureException(task, e);
                    throw stfe;
                }

                return result;
            }

        });
        taskQueue.put(ft);
        terminationToken.reservations.incrementAndGet();
        return ft;
    }

    private SubTaskFailureException newSubTaskFailureException(final T subTask,
            Exception cause) {
        RetryInfo<T, V> retryInfo =
                new RetryInfo<T, V>(subTask, new Callable<V>() {
                    @Override
                    public V call() throws Exception {
                        V result;
                        result = doProcess(subTask);
                        return result;
                    }

                });

        return new SubTaskFailureException(retryInfo, cause);
    }

    /**
     * 留给子类实现。用于实现子任务的处理逻辑。
     * 
     * @param task
     *            子任务
     * @return 子任务的处理结果
     * @throws Exception
     */
    protected abstract V doProcess(T task) throws Exception;

    @Override
    protected void doRun() throws Exception {
        try {
            Runnable task = taskQueue.take();
            task.run();
        } finally {
            terminationToken.reservations.decrementAndGet();
        }

    }

    @Override
    public void init() {
        start();
    }

    @Override
    public void shutdown() {
        terminate(true);
    }
}