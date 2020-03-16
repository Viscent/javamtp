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

package io.github.viscent.mtpattern.ch1;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WorkerThread {

	public static void main(String[] args) {
		Helper helper = new Helper();
		helper.init();

		// 此处，helper的客户端线程为main线程
		helper.submit("Something...");

	}

	static class Helper {
		private final BlockingQueue<String> workQueue = new ArrayBlockingQueue<String>(
		    100);

		// 用于处理队列workQueue中的任务的工作者线程
		private final Thread workerThread = new Thread() {

			@Override
			public void run() {
				String task = null;
				while (true) {
					try {
						task = workQueue.take();
					} catch (InterruptedException e) {
						break;
					}
					System.out.println(doProcess(task));

				}
			}

		};

		public void init() {
			workerThread.start();
		}

		protected String doProcess(String task) {
			return task + "->processed.";
		}

		public void submit(String task) {
			try {
				workQueue.put(task);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				;
			}
		}

	}

}
