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

import java.util.concurrent.TimeUnit;

/**
 * 对处理阶段的抽象。 负责对其输入进行处理，并将其输出作为下一个处理阶段的输入。
 * 
 * @author Viscent Huang
 *
 * @param <IN>
 *            输入类型
 * @param <OUT>
 *            输出类型
 */
public interface Pipe<IN, OUT> {
    /**
     * 设置当前Pipe实例的下一个Pipe实例。
     * 
     * @param nextPipe
     *            下一个Pipe实例
     */
    void setNextPipe(Pipe<?, ?> nextPipe);

    /**
     * 初始化当前Pipe实例对外提供的服务。
     * 
     * @param pipeCtx
     */
    void init(PipeContext pipeCtx);

    /**
     * 停止当前Pipe实例对外提供的服务。
     * 
     * @param timeout
     * @param unit
     */
    void shutdown(long timeout, TimeUnit unit);

    /**
     * 对输入元素进行处理，并将处理结果作为下一个Pipe实例的输入。
     */
    void process(IN input) throws InterruptedException;
}
