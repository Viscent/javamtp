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

package io.github.viscent.mtpattern.ch13.pipeline;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 支持并行处理的Pipe实现类。 该类对其每个输入元素生成一组子任务，并以并行的方式去执行这些子任务。
 * 各个子任务的执行结果会被合并为相应输入元素的输出结果。
 * 
 * @author Viscent Huang
 *
 * @param <IN>
 *            输入类型
 * @param <OUT>
 *            输出类型
 * 
 * @param <V>
 *            并行子任务的处理结果类型
 */
public abstract class AbstractParallelPipe<IN, OUT, V> extends
        AbstractPipe<IN, OUT> {
    private final ExecutorService executorService;

    public AbstractParallelPipe(BlockingQueue<IN> queue,
            ExecutorService executorService) {
        super();
        this.executorService = executorService;
    }

    /**
     * 留给子类实现。用于根据指定的输入元素input构造一组子任务。
     * 
     * @param input
     *            输入元素
     * @param <V>
     *            子任务的处理结果类型
     * @return 一组子任务
     */
    protected abstract List<Callable<V>> buildTasks(IN input) throws Exception;

    /**
     * 留给子类实现。对各个子任务的处理结果进行合并，形成相应输入元素的输出结果。
     * 
     * @param subTaskResults
     *            子任务处理结果列表
     * @return 相应输入元素的处理结果
     * @throws Exception
     */
    protected abstract OUT combineResults(List<Future<V>> subTaskResults)
            throws Exception;

    /**
     * 以并行的方式执行一组子任务。
     * 
     * @param tasks
     *            一组子任务
     * @return 一组可以借以获取并行任务中的各个任务的处理结果的Promise（参见第6章，Promise模式）实例。
     */
    protected List<Future<V>> invokeParallel(List<Callable<V>> tasks)
            throws Exception {
        return executorService.invokeAll(tasks);
    }

    @Override
    protected OUT doProcess(final IN input) throws PipeException {
        OUT out = null;
        try {
            out = combineResults(invokeParallel(buildTasks(input)));
        } catch (Exception e) {
            throw new PipeException(this, input, "Task failed", e);
        }
        return out;
    }

}