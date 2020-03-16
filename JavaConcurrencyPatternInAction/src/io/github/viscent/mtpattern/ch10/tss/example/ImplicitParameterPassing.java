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

import java.util.concurrent.atomic.AtomicInteger;

import io.github.viscent.util.Debug;

public class ImplicitParameterPassing {

    public static void main(String[] args) throws InterruptedException {
        ClientThread thread;
        BusinessService bs = new BusinessService();
        for (int i = 0; i < Integer.valueOf(args[0]); i++) {
            thread = new ClientThread("test", bs);
            thread.start();
            thread.join();
        }

    }

}

class ClientThread extends Thread {
    private final String message;
    private final BusinessService bs;
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    public ClientThread(String message, BusinessService bs) {
        this.message = message;
        this.bs = bs;
    }

    @Override
    public void run() {
        Context.INSTANCE.setTransactionId(SEQ.getAndIncrement());
        bs.service(message);
    }

}

class Context {
    static final ThreadLocal<Integer> TS_OBJECT_PROXY =
            new ThreadLocal<Integer>();

    // Context类的唯一实例
    public static final Context INSTANCE = new Context();

    // 私有构造器
    private Context() {

    }

    public Integer getTransactionId() {
        return TS_OBJECT_PROXY.get();
    }

    public void setTransactionId(Integer transactionId) {
        TS_OBJECT_PROXY.set(transactionId);
    }

    public void reset() {
        TS_OBJECT_PROXY.remove();
    }

}

class BusinessService {

    public void service(String message) {
        int transactionId = Context.INSTANCE.getTransactionId();
        Debug.info("processing transaction " + transactionId
                + "'s message:" + message);
    }
}
