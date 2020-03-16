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

package io.github.viscent.mtpattern.ch8.activeobject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Active Object模式的Proxy参与者的可复用实现。 模式角色：ActiveObject.Proxy
 * 
 * @author Viscent Huang
 */
public abstract class ActiveObjectProxy {
    /**
     * 生成一个实现指定接口的Active Object proxy实例。
     * 对interf所定义的异步方法的调用会被转发到servant的相应doXXX方法。
     * 
     * @param interf
     *            要实现的Active Object接口
     * @param servant
     *            Active Object的Servant参与者实例
     * @param scheduler
     *            Active Object的Scheduler参与者实例
     * @return Active Object的Proxy参与者实例
     */
    public static <T> T newInstance(Class<T> interf, Object servant,
            ExecutorService scheduler) {

        @SuppressWarnings("unchecked")
        T f = (T) Proxy.newProxyInstance(interf.getClassLoader(),
                new Class[] { interf },
                new DispatchInvocationHandler(servant, scheduler));
        return f;
    }
}

class DispatchInvocationHandler implements InvocationHandler {
    private final Object delegate;
    private final ExecutorService scheduler;

    public DispatchInvocationHandler(Object delegate,
            ExecutorService executorService) {
        this.delegate = delegate;
        this.scheduler = executorService;
    }

    private String makeDelegateMethodName(final Method method,
            final Object[] arg) {
        String name = method.getName();
        name = "do" + Character.toUpperCase(name.charAt(0)) + name.substring(1);

        return name;
    }

    @Override
    public Object invoke(final Object proxy, final Method method,
            final Object[] args) throws Throwable {

        Object returnValue = null;
        final Object delegate = this.delegate;
        final Method delegateMethod;

        // 如果拦截到的被调用方法是异步方法，则将其转发到相应的doXXX方法
        if (Future.class.isAssignableFrom(method.getReturnType())) {
            delegateMethod = delegate.getClass().getMethod(
                    makeDelegateMethodName(method, args),
                    method.getParameterTypes());

            final ExecutorService scheduler = this.scheduler;

            Callable<Object> methodRequest = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Object rv = null;
                    try {
                        rv = delegateMethod.invoke(delegate, args);
                    } catch (IllegalArgumentException | IllegalAccessException
                            | InvocationTargetException e) {
                        throw new Exception(e);
                    }
                    return rv;
                }
            };
            Future<Object> future = scheduler.submit(methodRequest);
            returnValue = future;

        } else {

            // 若拦截到的方法调用不是异步方法，则直接转发
            delegateMethod = delegate.getClass().getMethod(method.getName(),
                    method.getParameterTypes());
            returnValue = delegateMethod.invoke(delegate, args);
        }

        return returnValue;
    }
}
