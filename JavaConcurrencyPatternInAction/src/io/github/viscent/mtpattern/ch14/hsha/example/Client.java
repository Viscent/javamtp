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

package io.github.viscent.mtpattern.ch14.hsha.example;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import io.github.viscent.mtpattern.ch5.tpt.example.AlarmType;
import io.github.viscent.util.Debug;

public class Client {

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        Debug.info("started.");

        final AlarmMgr alarmMgr = AlarmMgr.getInstance();
        alarmMgr.init();

        final String alarmId = "10000020";
        final String alarmExtraInfo = "name1=test;name2=value2";

        Thread t1;

        int count1 = 10;
        final Timer timer = new Timer(true);
        final CyclicBarrier cbr = new CyclicBarrier(count1, new Runnable() {

            @Override
            public void run() {

                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        alarmMgr.sendAlarm(AlarmType.RESUME, alarmId,
                                alarmExtraInfo);
                        timer.cancel();
                    }

                }, 50);

            }// end of run

        });

        for (int i = 0; i < count1; i++) {
            t1 = new Thread() {
                @Override
                public void run() {
                    try {
                        cbr.await();
                    } catch (InterruptedException e) {
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    for (int j = 0; j < 10; j++) {

                        alarmMgr.sendAlarm(AlarmType.FAULT, alarmId,
                                alarmExtraInfo);
                    }
                }
            };
            t1.start();
        }

        // alarmMgr.disconnect();

        Random rnd = new Random();
        Timer timer1 = new Timer(true);
        timer1.schedule(new TimerTask() {

            @Override
            public void run() {
                (new Thread() {

                    @Override
                    public void run() {
                        for (int i = 0; i < 20; i++) {
                            alarmMgr.sendAlarm(AlarmType.FAULT, alarmId,
                                    alarmExtraInfo);
                        }

                    }

                }).start();

            }

        }, rnd.nextInt(150));

        timer1.schedule(new TimerTask() {

            @Override
            public void run() {
                alarmMgr.shutdown();

            }

        }, 5000);

    }

}
