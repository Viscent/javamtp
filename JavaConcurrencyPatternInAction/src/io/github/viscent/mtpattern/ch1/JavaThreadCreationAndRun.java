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

public class JavaThreadCreationAndRun {

	public static void main(String[] args) {
		System.out.println("The main method was executed by thread:"
		    + Thread.currentThread().getName());
		Helper helper = new Helper("Java Thread AnyWhere");
		
		//创建一个线程
		Thread thread = new Thread(helper);
		
		//设置线程名
		thread.setName("A-Worker-Thread");
		
		//启动线程
		thread.start();
	}

	static class Helper implements Runnable {
		private final String message;

		public Helper(String message) {
			this.message = message;
		}

		private void doSomething(String message) {
			System.out.println("The doSomething method was executed by thread:"
			    + Thread.currentThread().getName());

			System.out.println("Do something with " + message);
		}

		@Override
		public void run() {
			doSomething(message);

		}
	}//end of Helper
}
