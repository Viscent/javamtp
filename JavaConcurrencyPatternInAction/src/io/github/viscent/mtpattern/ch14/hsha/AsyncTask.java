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

package io.github.viscent.mtpattern.ch14.hsha;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.viscent.mtpattern.ch9.threadpool.example.ReEnqueueRejectedExecutionHandler;

/**
 * Half-sync/Half-async模式的可复用实现。
 * 
 * @author Viscent Huang
 *
 * @param <V>
 *            同步任务的处理结果类型
 */
public abstract class AsyncTask<V> {

    // 相当于Half-sync/Half-async模式的同步层：用于执行异步层提交的任务
    private volatile Executor executor;
    private final static ExecutorService DEFAULT_EXECUTOR;

    static {
        DEFAULT_EXECUTOR = new ThreadPoolExecutor(1, 1, 8, TimeUnit.HOURS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "AsyncTaskDefaultWorker");
                                // 使该线程在Java虚拟机关闭时自动停止
                        thread.setDaemon(true);
                        return thread;
                    }

                }, /**
                    * 该RejectedExecutionHandler支持重试。 当任务被ThreadPoolExecutor拒绝时，
                    * 该RejectedExecutionHandler支持 重新将任务放入ThreadPoolExecutor
                    * 的工作队列（这意味着，此时客户端代码 需要等待ThreadPoolExecutor的队列非满）。
                    */
                new ReEnqueueRejectedExecutionHandler());
    }

    public AsyncTask(Executor executor) {
        this.executor = executor;
    }

    public AsyncTask() {
        this(DEFAULT_EXECUTOR);
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /**
     * 留给子类实现耗时较短的任务，默认实现什么也不做。
     * 
     * @param params
     *            客户端代码调用dispatch方法时所传递的参数列表
     */
    protected void onPreExecute(Object... params) {
        // 什么也不做
    }

    /**
     * 留给子类实现。用于实现同步任务执行结束后所需执行的操作。 默认实现什么也不做。
     * 
     * @param result
     *            同步任务的处理结果
     */
    protected void onPostExecute(V result) {
        // 什么也不做
    }

    protected void onExecutionError(Exception e) {
        e.printStackTrace();
    }

    /**
     * 留给子类实现耗时较长的任务（同步任务），由后台线程负责调用。
     * 
     * @param params
     *            客户端代码调用dispatch方法时所传递的参数列表
     * @return 同步任务的处理结果
     */
    protected abstract V doInBackground(Object... params);

    /**
     * 对外（其子类）暴露的服务方法。 该类的子类需要定义一个比该方法命名更为具体的服务方法（如downloadLargeFile）。
     * 该命名具体的服务方法（如downloadLargeFile）可直接调用该方法。
     * 
     * @param params
     *            客户端代码传递的参数列表
     * @return 可借以获取任务处理结果的Promise（参见第6章，Promise模式）实例。
     */
    protected Future<V> dispatch(final Object... params) {
        FutureTask<V> ft = null;

        // 进行异步层初步处理
        onPreExecute(params);

        Callable<V> callable = new Callable<V>() {
            @Override
            public V call() throws Exception {
                V result;
                result = doInBackground(params);
                return result;
            }

        };

        ft = new FutureTask<V>(callable) {

            @Override
            protected void done() {
                try {
                    onPostExecute(this.get());
                } catch (InterruptedException e) {
                    onExecutionError(e);
                } catch (ExecutionException e) {
                    onExecutionError(e);
                }
            }

        };

        // 提交任务到同步层处理
        executor.execute(ft);

        return ft;
    }

}