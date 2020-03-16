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

package io.github.viscent.mtpattern.ch8.activeobject.example;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;
import io.github.viscent.mtpattern.ch5.tpt.DelegatingTerminatableThread;

public class CaseRunner {

    public static void main(String[] args) {
        int numRequestThreads = Runtime.getRuntime().availableProcessors();
        int targetTPS = 50;
        float duration = 12;// 单位：秒

        final RequestSender reqSender = new RequestSender(
                (int) duration * targetTPS);

        final Set<AbstractTerminatableThread> requestThreads = new HashSet<AbstractTerminatableThread>();

        AbstractTerminatableThread requestThread;
        for (int i = 0; i < numRequestThreads; i++) {
            requestThread = DelegatingTerminatableThread.of(reqSender);
            requestThreads.add(requestThread);
            requestThread.start();
        }

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Terminating worker threads....");
                for (AbstractTerminatableThread att : requestThreads) {
                    att.terminate(true);
                }
                reqSender.shutdown();
            }
        }, (long) duration * 1000L);

    }

    static class RequestSender implements Runnable {
        final AtomicInteger totalCount = new AtomicInteger();
        private final RequestPersistence persistence;
        private final Attachment attachment;

        public RequestSender(int n) {
            totalCount.set(n);
            persistence = AsyncRequestPersistence.getInstance();
            attachment = new Attachment();
            try {
                // 附件文件，可根据实际情况修改！
                URL url = CaseRunner.class.getClassLoader().getResource(
                        "io/github/viscent/mtpattern/ch8/activeobject/example/attachment.jpg");
                attachment.setContentType("image/jpeg");
                attachment
                        .setContent(Files.readAllBytes(Paths.get(url.toURI())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            int remainingCount;
            while ((remainingCount = totalCount.getAndDecrement()) > 0) {
                MMSDeliverRequest request = new MMSDeliverRequest();
                request.setTransactionID(String.valueOf(remainingCount));
                request.setSenderAddress("13612345678");
                request.setTimeStamp(new Date());
                request.setExpiry((new Date().getTime() + 3600000) / 1000);

                request.setSubject("Hi");
                request.getRecipient().addTo("776");
                request.setAttachment(attachment);
                persistence.store(request);
            }
        }

        public void shutdown() {
            try {
                persistence.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}