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

package io.github.viscent.mtpattern.ch4.gs.example;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import io.github.viscent.mtpattern.ch4.gs.Blocker;
import io.github.viscent.mtpattern.ch4.gs.ConditionVarBlocker;
import io.github.viscent.mtpattern.ch4.gs.GuardedAction;
import io.github.viscent.mtpattern.ch4.gs.Predicate;
import io.github.viscent.util.Debug;

/**
 * 本程序是为了演示“嵌套监视器锁死“而写的，因此本程序需要通过手工终止进程才能结束。
 * 
 * @author Viscent Huang
 *
 */
public class NestedMonitorLockoutExample {

    public static void main(String[] args) {
        final Helper helper = new Helper();
        Debug.info("Before calling guaredMethod.");

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                String result;
                result = helper.xGuarededMethod("test");
                Debug.info(result);
            }

        });
        t.start();

        final Timer timer = new Timer();

        // 延迟50ms调用helper.stateChanged方法
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                helper.xStateChanged();
                timer.cancel();
            }

        }, 50, 10);

    }

    private static class Helper {
        private volatile boolean isStateOK = false;
        private final Predicate stateBeOK = new Predicate() {

            @Override
            public boolean evaluate() {
                return isStateOK;
            }

        };

        private final Blocker blocker = new ConditionVarBlocker();

        public synchronized String xGuarededMethod(final String message) {
            GuardedAction<String> ga = new GuardedAction<String>(stateBeOK) {

                @Override
                public String call() throws Exception {
                    return message + "->received.";
                }

            };
            String result = null;
            try {
                result = blocker.callWithGuard(ga);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        public synchronized void xStateChanged() {
            try {
                blocker.signalAfter(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        isStateOK = true;
                        Debug.info("state ok.");
                        return Boolean.TRUE;
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}