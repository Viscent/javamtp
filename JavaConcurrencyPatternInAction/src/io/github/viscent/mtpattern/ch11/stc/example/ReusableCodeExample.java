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

package io.github.viscent.mtpattern.ch11.stc.example;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.github.viscent.mtpattern.ch11.stc.AbstractSerializer;
import io.github.viscent.mtpattern.ch11.stc.TaskProcessor;

public class ReusableCodeExample {

	public static void main(String[] args) throws InterruptedException,
	    ExecutionException {
		SomeService ss = new SomeService();
		ss.init();
		Future<String> result = ss.doSomething("Serial Thread Confinement", 1);

		Thread.sleep(50);

		System.out.println(result.get());

		ss.shutdown();

	}

	private static class Task {
		public final String message;
		public final int id;

		public Task(String message, int id) {
			this.message = message;
			this.id = id;
		}
	}

	private static class SomeService extends AbstractSerializer<Task, String> {

		public SomeService() {
			super(new ArrayBlockingQueue<Runnable>(100),
			    new TaskProcessor<Task, String>() {

				    @Override
				    public String doProcess(Task task) throws Exception {
					    System.out.println("[" + task.id + "]:" + task.message);
					    return task.message + " accepted.";
				    }

			    });
		}

		@Override
		protected Task makeTask(Object... params) {
			String message = (String) params[0];
			int id = (Integer) params[1];

			return new Task(message, id);
		}

		public Future<String> doSomething(String message, int id)
		    throws InterruptedException {
			Future<String> result = null;

			result = service(message, id);

			return result;
		}

	}

}
