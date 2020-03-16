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

import java.util.Map;

public class ChargingRequestFactory extends AbstractRequestFactory {
    public ChargingRequestFactory(Map<String, Integer> respDelayConf,
            int timestampMaxOffset) {
        super(respDelayConf, timestampMaxOffset);
    }

    @Override
    public SimulatedRequest newRequest() {
        return new SimulatedRequest() {

            @Override
            public void printLogs(Logger logger) {
                final int internalDelay = genInternalDelay();
                LogEntry entry = new LogEntry();
                long timeStamp = nextRequestTimestamp();
                entry.timeStamp = timeStamp;
                entry.recordType = "request";

                entry.interfaceType = "SOAP";
                entry.interfaceName = "Chg";
                entry.operationName = "getPrice";
                entry.srcDevice = "OSG";
                entry.dstDevice = "ESB";

                int traceId = seq.getAndIncrement();
                int originalTId = traceId;

                entry.traceId = "0020" + traceIdFormatter.format(traceId);

                logger.printLog(entry);

                timeStamp += internalDelay;
                entry.timeStamp = timeStamp;

                entry.recordType = "request";
                entry.operationName = "getPrice";
                entry.srcDevice = "ESB";
                entry.dstDevice = "BSS";
                traceId = traceId + 1;
                entry.traceId = "0021" + traceIdFormatter.format(traceId);
                logger.printLog(entry);

                timeStamp += genResponseDelay("BSS");
                entry.timeStamp = timeStamp;
                entry.recordType = "response";
                entry.operationName = "getPriceRsp";
                entry.srcDevice = "BSS";
                entry.dstDevice = "ESB";
                traceId = traceId + 3;
                entry.traceId = "0021" + traceIdFormatter.format(traceId);
                logger.printLog(entry);

                timeStamp += internalDelay;
                entry.timeStamp = timeStamp;
                entry.recordType = "response";
                entry.operationName = "getLocationRsp";
                entry.srcDevice = "ESB";
                entry.dstDevice = "OSG";
                entry.traceId = "0020"
                        + traceIdFormatter.format(originalTId + 2);
                logger.printLog(entry);

            }

            @Override
            public String getInterfaceName() {
                return "Charging";
            }

        };
    }

}
