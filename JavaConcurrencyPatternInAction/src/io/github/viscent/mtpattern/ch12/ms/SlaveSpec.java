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

import java.util.concurrent.Future;

/**
 * 对 Master-Slave模式Slave参与者的抽象。
 * 
 * @author Viscent Huang
 *
 * @param <T>
 *            子任务类型
 * @param <V>
 *            子任务处理结果类型
 */
public interface SlaveSpec<T, V> {
    /**
     * 用于Master向其提交一个子任务。
     * 
     * @param task
     *            子任务
     * @return 可借以获取子任务处理结果的Promise（参见第6章，Promise模式）实例。
     * @throws InterruptedException
     */
    Future<V> submit(final T task) throws InterruptedException;

    /**
     * 初始化Slave实例提供的服务
     */
    void init();

    /**
     * 停止Slave实例对外提供的服务
     */
    void shutdown();
}