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

package io.github.viscent.mtpattern.ch10.tss;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 支持统一清理不再被使用的ThreadLocal变量的ThreadLocal子类。
 * 
 * @author Viscent Huang
 * 
 * @param <T> 相应的线程特有对象类型
 */
public class ManagedThreadLocal<T> extends ThreadLocal<T> {

	/*
	 * 使用弱引用，防止内存泄漏。
	 * 使用volatile修饰保证内存可见性。
	 */
	private static volatile Queue<WeakReference<ManagedThreadLocal<?>>> instances = new ConcurrentLinkedQueue<WeakReference<ManagedThreadLocal<?>>>();

	private volatile ThreadLocal<T> threadLocal;

	private ManagedThreadLocal(final InitialValueProvider<T> ivp) {

		this.threadLocal = new ThreadLocal<T>() {

			@Override
			protected T initialValue() {
				return ivp.initialValue();
			}

		};
	}

	public static <T> ManagedThreadLocal<T> newInstance(
	    final InitialValueProvider<T> ivp) {
		ManagedThreadLocal<T> mtl = new ManagedThreadLocal<T>(ivp);

		// 使用弱引用来引用ThreadLocalProxy实例，防止内存泄漏。
		instances.add(new WeakReference<ManagedThreadLocal<?>>(mtl));
		return mtl;
	}
	
	public static <T> ManagedThreadLocal<T> newInstance() {
		return newInstance(new ManagedThreadLocal.InitialValueProvider<T>());
	}

	public T get() {
		return threadLocal.get();
	}

	public void set(T value) {
		threadLocal.set(value);
	}

	public void remove() {
		if (null != threadLocal) {
			threadLocal.remove();
			threadLocal = null;
		}
	}

	/**
	 * 清理该类所管理的所有ThreadLocal实例。
	 */
	public static void removeAll() {
		WeakReference<ManagedThreadLocal<?>> wrMtl;
		ManagedThreadLocal<?> mtl;
		while (null != (wrMtl = instances.poll())) {
			mtl = wrMtl.get();
			if (null != mtl) {
				mtl.remove();
			}
		}
	}

	public static class InitialValueProvider<T> {
		protected T initialValue(){
			
			//默认值为null
			return null;
		}
	}

}
