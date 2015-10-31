/*
授权声明：
本源码系《Java多线程编程实战指南（设计模式篇）》一书（ISBN：978-1-7-121-27006-2，以下称之为“原书”）的配套源码，
欲了解本代码的更多细节，请参考原书。
本代码仅为原书的配套说明之用，并不附带任何承诺（如质量保证和收益）。
以任何形式将本代码之部分或者全部用于营利性用途需经版权人书面同意。
将本代码之部分或者全部用于非营利性用途需要在代码中保留本声明。
任何对本代码的修改需在代码中以注释的形式注明修改人、修改时间以及修改内容。
本代码可以从下URL下载：
https://github.com/Viscent/javamtp
http://www.broadview.com.cn/27006
*/

package io.github.viscent.mtpattern.ch8.activeobject.example;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CaseRunner {

	public static void main(String[] args) {
		int numRequestThreads = Runtime.getRuntime().availableProcessors();
		int targetTPS = 200;

		// 每秒中提交的请求数量
		int requestsPerInterval = targetTPS / numRequestThreads;
		final AtomicInteger counter = new AtomicInteger(0);

		final Set<AbstractTerminatableThread> requestThreads = new HashSet<AbstractTerminatableThread>();

		AbstractTerminatableThread requestThread;
		for (int i = 0; i < numRequestThreads; i++) {
			requestThread = new RequestSenderThread(requestsPerInterval, counter);
			requestThreads.add(requestThread);
			requestThread.start();
		}

		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				for (AbstractTerminatableThread att : requestThreads) {
					att.terminate();
				}

			}

		}, 5 * 60 * 1000);
	}

	static class RequestSenderThread extends AbstractTerminatableThread {

		private final int requestsPerInterval;
		private final RequestPersistence persistence;
		private final ThreadPoolExecutor executor;
		private final Attachment attachment;
		private final AtomicInteger counter;

		public RequestSenderThread(int requestsPerInterval, AtomicInteger counter) {

			this.requestsPerInterval = requestsPerInterval;
			this.counter = counter;
			persistence = AsyncRequestPersistence.getInstance();
			executor = new ThreadPoolExecutor(80, 200, 60 * 3600, TimeUnit.SECONDS,
			    new ArrayBlockingQueue<Runnable>(300));
			attachment = new Attachment();
			try {

				// 附件文件，可根据实际情况修改！
				URL url = CaseRunner.class
				    .getClassLoader()
				    .getResource(
				        "io/github/viscent/mtpattern/ch8/activeobject/example/attachment.png");
				File file = new File(url.getFile());
				ByteBuffer contentBuf = ByteBuffer.allocate((int) file.length());
				FileInputStream fin = new FileInputStream(file);
				try {
					fin.getChannel().read(contentBuf);
				} finally {
					fin.close();
				}

				attachment.setContentType("image/png");
				attachment.setContent(contentBuf.array());
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void doRun() {
			for (int i = 0; i < requestsPerInterval; i++) {

				executor.execute(new Runnable() {

					@Override
					public void run() {
						MMSDeliverRequest request = new MMSDeliverRequest();
						request.setTransactionID(String.valueOf(counter.incrementAndGet()));
						request.setSenderAddress("13612345678");
						request.setTimeStamp(new Date());
						request.setExpiry((new Date().getTime() + 3600000) / 1000);

						request.setSubject("Hi");
						request.getRecipient().addTo("776");
						request.setAttachment(attachment);

						persistence.store(request);

					}
				});

			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void doCleanup(Exception cause) {
			executor.shutdownNow();
		}

	}

}