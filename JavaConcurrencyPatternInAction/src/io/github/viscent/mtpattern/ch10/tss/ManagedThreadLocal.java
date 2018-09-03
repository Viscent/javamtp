/*
授权声明：
本源码系《Java多线程编程实战指南（设计模式篇）》一书（ISBN：978-7-121-27006-2，以下称之为“原书”）的配套源码，
欲了解本代码的更多细节，请参考原书。
本代码仅为原书的配套说明之用，并不附带任何承诺（如质量保证和收益）。
以任何形式将本代码之部分或者全部用于营利性用途需经版权人书面同意。
将本代码之部分或者全部用于非营利性用途需要在代码中保留本声明。
任何对本代码的修改需在代码中以注释的形式注明修改人、修改时间以及修改内容。
本代码可以从以下网址下载：
https://github.com/Viscent/javamtp
http://www.broadview.com.cn/27006
*/

package io.github.viscent.mtpattern.ch10.tss;

import java.lang.ref.WeakReference;
import java.util.AbstractMap.SimpleEntry;

/**
 * 能够避免内存泄漏的ThreadLocal子类。
 * 
 * @author Viscent Huang
 * 
 * @param <T>
 *            相应的线程特有对象类型
 */
public class ManagedThreadLocal<T> extends ThreadLocal<T> {
    private final ThreadLocal<SimpleEntry<String, T>> storageHelper;
    private volatile WeakReference<SimpleEntry<String, ?>> wrTSOWrapper;

    {
        storageHelper = new ThreadLocal<SimpleEntry<String, T>>() {
            @Override
            protected SimpleEntry<String, T> initialValue() {
                SimpleEntry<String, T> tsoWrapper = new SimpleEntry<>(null,
                        null);
                wrTSOWrapper = new WeakReference<SimpleEntry<String, ?>>(
                        tsoWrapper);
                return tsoWrapper;
            }
        };

    }

    @Override
    public T get() {
        // entry是一个线程特有对象
        SimpleEntry<String, T> entry = storageHelper.get();
        T v = entry.getValue();
        if (null == v) {
            v = this.initialValue();
            entry.setValue(v);
        }
        return v;
    }

    @Override
    public void set(T value) {
        SimpleEntry<String, T> entry = storageHelper.get();
        // 对线程特有对象进行更新操作无需使用任何线程同步措施
        entry.setValue(value);
    }

    @Override
    public void remove() {
        storageHelper.remove();
    }

    public void destroy() {
        final WeakReference<SimpleEntry<String, ?>> wrTSOWrapper = this.wrTSOWrapper;
        if (null != wrTSOWrapper) {
            SimpleEntry<String, ?> entry = wrTSOWrapper.get();
            if (null != entry) {
                entry.setValue(null);
            }
        }
    }
}
