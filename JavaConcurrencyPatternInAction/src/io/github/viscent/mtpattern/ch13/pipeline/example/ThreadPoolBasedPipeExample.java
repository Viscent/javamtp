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

package io.github.viscent.mtpattern.ch13.pipeline.example;

import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.viscent.mtpattern.ch13.pipeline.AbstractPipe;
import io.github.viscent.mtpattern.ch13.pipeline.Pipe;
import io.github.viscent.mtpattern.ch13.pipeline.PipeException;
import io.github.viscent.mtpattern.ch13.pipeline.SimplePipeline;
import io.github.viscent.util.Debug;
import io.github.viscent.util.Tools;

public class ThreadPoolBasedPipeExample {

    public static void main(String[] args) {
        final ThreadPoolExecutor executorSerivce;
        executorSerivce =
                new ThreadPoolExecutor(1, Runtime.getRuntime()
                        .availableProcessors() * 2, 60, TimeUnit.MINUTES,
                        new SynchronousQueue<Runnable>(),
                        new ThreadPoolExecutor.CallerRunsPolicy());

        final SimplePipeline<String, String> pipeline =
                new SimplePipeline<String, String>();
        Pipe<String, String> pipe = new AbstractPipe<String, String>() {

            @Override
            protected String doProcess(String input) throws PipeException {
                String result =
                        input + "->[pipe1," + Thread.currentThread().getName()
                                + "]";
                Debug.info(result);
                return result;
            }
        };

        pipeline.addAsThreadPoolBasedPipe(pipe, executorSerivce);

        pipe = new AbstractPipe<String, String>() {

            @Override
            protected String doProcess(String input) throws PipeException {
                String result =
                        input + "->[pipe2," + Thread.currentThread().getName()
                                + "]";
                Debug.info(result);
                try {
                    Thread.sleep(new Random().nextInt(100));
                } catch (InterruptedException e) {
                    ;
                }
                return result;
            }
        };

        pipeline.addAsThreadPoolBasedPipe(pipe, executorSerivce);

        pipe = new AbstractPipe<String, String>() {

            @Override
            protected String doProcess(String input) throws PipeException {
                String result =
                        input + "->[pipe3," + Thread.currentThread().getName()
                                + "]";
                Debug.info(result);

                // 模拟实际操作的耗时
                Tools.randomPause(200, 90);
                return result;
            }

            @Override
            public void shutdown(long timeout, TimeUnit unit) {

                // 在最后一个Pipe中关闭线程池
                executorSerivce.shutdown();
                try {
                    executorSerivce.awaitTermination(timeout, unit);
                } catch (InterruptedException e) {
                    ;
                }
            }
        };

        pipeline.addAsThreadPoolBasedPipe(pipe, executorSerivce);

        pipeline.init(pipeline.newDefaultPipelineContext());

        int N = 100;
        try {
            for (int i = 0; i < N; i++) {
                pipeline.process("Task-" + i);
            }
        } catch (IllegalStateException e) {
            ;
        } catch (InterruptedException e) {
            ;
        }

        pipeline.shutdown(10, TimeUnit.SECONDS);

    }

}
