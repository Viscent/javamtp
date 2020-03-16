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

package io.github.viscent.mtpattern.ch7.pc.exmaple;

import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;
import io.github.viscent.mtpattern.ch5.tpt.TerminationToken;
import io.github.viscent.mtpattern.ch7.pc.WorkStealingChannel;
import io.github.viscent.mtpattern.ch7.pc.WorkStealingEnabledChannel;

/**
 * 工作窃取算法示例。 该类使用Two-phase Termination模式（参见第5章）。
 * 
 * @author Viscent Huang
 *
 */
public class WorkStealingExample {
	private final WorkStealingEnabledChannel<String> channel;
	private final TerminationToken token = new TerminationToken();

	public WorkStealingExample() {
		int nCPU = Runtime.getRuntime().availableProcessors();
		int consumerCount = nCPU / 2 + 1;

		@SuppressWarnings("unchecked")
		BlockingDeque<String>[] managedQueues 
												= new LinkedBlockingDeque[consumerCount];

		// 该通道实例对应了多个队列实例managedQueues
		channel = new WorkStealingChannel<String>(managedQueues);

		Consumer[] consumers = new Consumer[consumerCount];
		for (int i = 0; i < consumerCount; i++) {
			managedQueues[i] = new LinkedBlockingDeque<String>();
			consumers[i] = new Consumer(token, managedQueues[i]);

		}

		for (int i = 0; i < nCPU; i++) {
			new Producer().start();
		}

		for (int i = 0; i < consumerCount; i++) {

			consumers[i].start();
		}

	}

	public void doSomething() {

	}

	public static void main(String[] args) throws InterruptedException {
		WorkStealingExample wse;
		wse = new WorkStealingExample();

		wse.doSomething();
		Thread.sleep(3500);

	}

	private class Producer extends AbstractTerminatableThread {
		private int i = 0;

		@Override
		protected void doRun() throws Exception {
			channel.put(String.valueOf(i++));
			token.reservations.incrementAndGet();
		}

	}

	private class Consumer extends AbstractTerminatableThread {
		private final BlockingDeque<String> workQueue;

		public Consumer(TerminationToken token, BlockingDeque<String> workQueue) {
			super(token);
			this.workQueue = workQueue;
		}

		@Override
		protected void doRun() throws Exception {

			/*
			 * WorkStealingEnabledChannel接口的take(BlockingDequepreferedQueue)方法
			 * 实现了工作窃取算法
			 */
			String product = channel.take(workQueue);

			System.out.println("Processing product:" + product);

			// 模拟执行真正操作的时间消耗
			try {
				Thread.sleep(new Random().nextInt(50));
			} catch (InterruptedException e) {
				;
			} finally {
				token.reservations.decrementAndGet();
			}

		}

	}

}
