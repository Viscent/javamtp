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

package io.github.viscent.mtpattern.ch10.tss.example;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.viscent.util.Debug;

public class SmsVerficationCodeSender {
    private static final ExecutorService EXECUTOR =
            new ThreadPoolExecutor(1,
                    Runtime.getRuntime().availableProcessors(), 60,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r, "VerfCodeSender");
                            t.setDaemon(true);
                            return t;
                        }

                    }, new ThreadPoolExecutor.DiscardPolicy());

    public static void main(String[] args) {
        SmsVerficationCodeSender client = new SmsVerficationCodeSender();

        client.sendVerificationSms("18912345678");
        client.sendVerificationSms("18712345679");
        client.sendVerificationSms("18612345676");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            ;
        }
    }

    /**
     * 生成并下发验证码短信到指定的手机号码。
     * 
     * @param msisdn
     *            短信接收方号码。
     */
    public void sendVerificationSms(final String msisdn) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 生成强随机数验证码
                int verificationCode =
                        ThreadSpecificSecureRandom.INSTANCE
                                .nextInt(999999);
                DecimalFormat df = new DecimalFormat("000000");
                String txtVerCode = df.format(verificationCode);

                // 发送验证码短信
                sendSms(msisdn, txtVerCode);
            }

        };

        EXECUTOR.submit(task);
    }

    private void sendSms(String msisdn, String verificationCode) {
        Debug.info("Sending verification code " + verificationCode + " to "
                        + msisdn);

        // 省略其他代码
    }

}
