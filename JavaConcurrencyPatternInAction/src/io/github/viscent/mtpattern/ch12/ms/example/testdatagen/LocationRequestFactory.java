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

package io.github.viscent.mtpattern.ch12.ms.example.testdatagen;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationRequestFactory implements RequestFactory {
	private final AtomicInteger seq=new AtomicInteger(0);
	@Override
	public SimulatedRequest newRequest() {
		// TODO Auto-generated method stub
		return new SimulatedRequest(){

			@Override
      public void printLogs(Logger logger) {
				LogEntry entry=new LogEntry();
	      entry.timeStamp=System.currentTimeMillis();
	      entry.recordType="request";
	      
	      entry.interfaceType="REST";
	      entry.interfaceName="Location";
	      entry.operationName="getLocation";
	      entry.srcDevice="OSG";
	      entry.dstDevice="ESB";
	      DecimalFormat df=new DecimalFormat("0000000");
	      
	      int traceId=seq.getAndIncrement();
	      int originalTId=traceId;
	      
	      entry.traceId="0020"+df.format(traceId);
	      
	      logger.printLog(entry);
	      
	      Random rnd = new Random();
	      entry.timeStamp+=rnd.nextInt(20);
	      entry.recordType="request";
	      entry.operationName="getLocation";
	      entry.srcDevice="ESB";
	      entry.dstDevice="NIG";
	      traceId=traceId+1;
	      entry.traceId="0021"+df.format(traceId);
	      logger.printLog(entry);
	      
	      
	      rnd = new Random();
	      entry.timeStamp+=rnd.nextInt(100);
	      entry.recordType="response";
	      entry.operationName="getLocationRsp";
	      entry.srcDevice="NIG";
	      entry.dstDevice="ESB";
	      traceId=traceId+3;
	      entry.traceId="0021"+df.format(traceId);
	      logger.printLog(entry);
	      
	      rnd = new Random();
	      entry.timeStamp+=rnd.nextInt(150);
	      entry.recordType="response";
	      entry.operationName="getLocationRsp";
	      entry.srcDevice="ESB";
	      entry.dstDevice="OSG";
	      entry.traceId="0020"+df.format(originalTId+2);
	      logger.printLog(entry);
	      
      }
			
		};
	}

}
