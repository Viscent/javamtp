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
package io.github.viscent.mtpattern.ch12.ms.example.testdatagen;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractRequestFactory implements RequestFactory {
    protected final Map<String, Integer> respDelayConf;
    protected final AtomicInteger seq = new AtomicInteger(0);
    protected final Random reqTimeRnd = new Random();
    protected final Random respDelayRnd = new Random();
    /**
     * 部件内部操作延时随机生成器
     */
    protected final Random internalDealyRnd = new Random();
    protected final long startTimestamp = new Date().getTime();
    private long nowTimestamp = startTimestamp;
    protected final DecimalFormat traceIdFormatter = new DecimalFormat(
            "0000000");
    /**
     * 统计周期。单位：秒
     */
    public static final int STAT_INTERVAL = 10;

    private final int maxRequestPerTimeslice;
    /**
     * 单位时间（10s）内产生的请求数
     */
    private int requestGeneratedInTimeSlice = 0;

    public AbstractRequestFactory(Map<String, Integer> respDelayConf,
            int maxRequestPerTimeslice) {
        super();
        this.respDelayConf = respDelayConf;
        this.maxRequestPerTimeslice = maxRequestPerTimeslice;
    }

    public long nextRequestTimestamp() {
        long requestTimestamp;

        requestTimestamp = nowTimestamp
                + reqTimeRnd.nextInt((STAT_INTERVAL - 1) * 1000)
                / 1000;
        if ((++requestGeneratedInTimeSlice) >= maxRequestPerTimeslice) {
            nowTimestamp = nowTimestamp + (STAT_INTERVAL * 1000);
            requestGeneratedInTimeSlice = 0;

        }

        return requestTimestamp;
    }

    public int genResponseDelay(String externalDevice) {
        return respDelayRnd.nextInt(respDelayConf.get(externalDevice));
    }

    public int genInternalDelay() {
        return internalDealyRnd.nextInt(respDelayConf.get("ESB"));
    }

}
