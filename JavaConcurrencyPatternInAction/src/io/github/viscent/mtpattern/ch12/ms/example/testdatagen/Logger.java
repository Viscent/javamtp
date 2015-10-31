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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.concurrent.atomic.AtomicLong;

public class Logger {
	
	//接口日志输出目标目录
	private static final String LOG_FILE_BASE_DIR = System
	    .getProperty("java.io.tmpdir") + "/tps/";
	private static final String TIME_STAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final String TIME_STAMP_FORMAT1 = "yyyyMMddHHmm";
	private int maxLines = 10000;
	private AtomicLong linesCount = new AtomicLong(maxLines);
	private PrintWriter cachedPwr = null;

	public void printLog(LogEntry entry) {
		// System.out.println(entry);
		// IP address
		PrintWriter pwr = this.getWriter();

		pwr.print(getUTCTimeStamp(Long.valueOf(entry.timeStamp), TIME_STAMP_FORMAT));
		pwr.print('|');

		pwr.print(entry.interfaceType);
		pwr.print('|');

		pwr.print(entry.recordType);
		pwr.print('|');

		pwr.print(entry.interfaceName);
		pwr.print('|');

		pwr.print(entry.operationName);
		pwr.print('|');

		pwr.print(entry.srcDevice);
		pwr.print('|');

		pwr.print(entry.dstDevice);
		pwr.print('|');

		pwr.print(entry.traceId);
		pwr.print('|');

		pwr.print(entry.selfIPAddress);
		pwr.print('|');

		pwr.print(entry.calling);
		pwr.print('|');

		pwr.print(entry.callee);
		pwr.println();
		long count = linesCount.incrementAndGet();
		if (count >= maxLines) {
			pwr.flush();
			pwr.close();
		}
	}

	private PrintWriter getWriter() {

		if (linesCount.get() >= maxLines) {
			try {
				cachedPwr = new PrintWriter(LOG_FILE_BASE_DIR + retrieveLogFileName());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			linesCount.set(0);
		}
		return cachedPwr;
	}

	private String retrieveLogFileName() {

		return "ESB_interface_"
		    + getUTCTimeStamp(System.currentTimeMillis(), TIME_STAMP_FORMAT1)
		    + ".log";
	}

	private static String getUTCTimeStamp(long timeStamp, String format) {
		SimpleTimeZone stz = new SimpleTimeZone(0, "UTC");
		Calendar calendar = Calendar.getInstance(stz);
		calendar.setTimeInMillis(timeStamp);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(stz);
		String tempTs = sdf.format(calendar.getTime());
		return tempTs;
	}
}
