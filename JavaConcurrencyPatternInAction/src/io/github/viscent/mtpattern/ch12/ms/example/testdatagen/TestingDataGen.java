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

package io.github.viscent.mtpattern.ch12.ms.example.testdatagen;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 生成本章实战案例运行时所需的接口日志文件。
 * 
 * @author Viscent Huang
 *
 */
public class TestingDataGen {

	public static void main(String[] args) {

		final long duration = 5 * 60; // 单位：s

		// 模拟接口日志文件中的请求速率
		final int tps = 100;
		final AbstractTerminatableThread att = new AbstractTerminatableThread() {
			final RequestFactory[] factories = new RequestFactory[] {
			    new SmsRequestFactory(), new ChargingRequestFactory(),
			    new LocationRequestFactory() };

			Logger logger = new Logger();
			int count = 0;

			@Override
			protected void doRun() throws Exception {
				RequestFactory rf;
				SimulatedRequest req;
				Random rnd = new Random();
				int i = 0;

				i = rnd.nextInt(factories.length);
				rf = factories[i];

				req = rf.newRequest();

				req.printLogs(logger);

				count++;

				if (0 == (count % tps)) {
					System.out.println(count);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					count = 0;
				}

			}

			@Override
			protected void doCleanup(Exception cause) {
				System.out.println("count:" + count + ",rate:" + (count / duration));
			}

		};

		att.setDaemon(false);

		att.start();

		final Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				att.terminate();

			}

		}, duration * 1000);

	}

}
