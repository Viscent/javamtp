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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class SimplePipeline<T, OUT> extends AbstractPipe<T, OUT> implements
        Pipeline<T, OUT> {
    private final static Logger logger = Logger.getLogger(SimplePipeline.class);
    private final Queue<Pipe<?, ?>> pipes = new LinkedList<Pipe<?, ?>>();

    private final ExecutorService helperExecutor;

    public SimplePipeline() {
        this(Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "SimplePipeline-Helper");
                t.setDaemon(true);
                return t;
            }

        }));

    }

    public SimplePipeline(final ExecutorService helperExecutor) {
        super();
        this.helperExecutor = helperExecutor;
    }

    @Override
    public void shutdown(long timeout, TimeUnit unit) {
        Pipe<?, ?> pipe;

        while (null != (pipe = pipes.poll())) {
            pipe.shutdown(timeout, unit);
        }

        helperExecutor.shutdown();

    }

    @Override
    protected OUT doProcess(T input) throws PipeException {
        // 什么也不做
        return null;
    }

    @Override
    public void addPipe(Pipe<?, ?> pipe) {

        // Pipe间的关联关系在init方法中建立
        pipes.add(pipe);
    }

    public <INPUT, OUTPUT> void addAsWorkerThreadBasedPipe(
            Pipe<INPUT, OUTPUT> delegate, int workerCount) {
        addPipe(new WorkerThreadPipeDecorator<INPUT, OUTPUT>(delegate,
                workerCount));
    }

    public <INPUT, OUTPUT> void addAsThreadPoolBasedPipe(
            Pipe<INPUT, OUTPUT> delegate, ExecutorService executorSerivce) {
        addPipe(new ThreadPoolPipeDecorator<INPUT, OUTPUT>(delegate,
                executorSerivce));
    }

    @Override
    public void process(T input) throws InterruptedException {
        @SuppressWarnings("unchecked")
        Pipe<T, ?> firstPipe = (Pipe<T, ?>) pipes.peek();

        firstPipe.process(input);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void init(final PipeContext ctx) {
        LinkedList<Pipe<?, ?>> pipesList = (LinkedList<Pipe<?, ?>>) pipes;
        Pipe<?, ?> prevPipe = this;
        for (Pipe<?, ?> pipe : pipesList) {
            prevPipe.setNextPipe(pipe);
            prevPipe = pipe;
        }
        helperExecutor.submit(new PipeInitTask(ctx, (List) pipes));
    }

    static class PipeInitTask implements Runnable {
        final List<Pipe<?, ?>> pipes;
        final PipeContext ctx;

        public PipeInitTask(PipeContext ctx, List<Pipe<?, ?>> pipes) {
            this.pipes = pipes;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            try {
                for (Pipe<?, ?> pipe : pipes) {
                    pipe.init(ctx);
                }
            } catch (Exception e) {
                logger.error("Failed to init pipe", e);
            }
        }

    }

    public PipeContext newDefaultPipelineContext() {
        return new PipeContext() {
            @Override
            public void handleError(final PipeException exp) {
                helperExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        logger.error("", exp);
                    }
                });
            }

        };
    }
}