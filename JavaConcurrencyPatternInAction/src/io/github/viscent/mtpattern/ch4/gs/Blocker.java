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

package io.github.viscent.mtpattern.ch4.gs;

import java.util.concurrent.Callable;

public interface Blocker {

    /**
     * 在保护条件成立时执行目标动作，否则阻塞当前线程，直到保护条件成立。
     * 
     * @param guardedAction
     *            带保护条件的目标动作
     * @return
     * @throws Exception
     */
    <V> V callWithGuard(GuardedAction<V> guardedAction) throws Exception;

    /**
     * 执行stateOperation所指定的操作后，决定是否唤醒本Blocker所暂挂的所有线程中的一个线程。
     * 
     * @param stateOperation
     *            更改状态的操作，其call方法的返回值为true时，该方法才会唤醒被暂挂的线程
     */
    void signalAfter(Callable<Boolean> stateOperation) throws Exception;

    void signal() throws InterruptedException;

    /**
     * 执行stateOperation所指定的操作后，决定是否唤醒本Blocker所暂挂的所有线程。
     * 
     * @param stateOperation
     *            更改状态的操作，其call方法的返回值为true时，该方法才会唤醒被暂挂的线程
     */
    void broadcastAfter(Callable<Boolean> stateOperation) throws Exception;
}