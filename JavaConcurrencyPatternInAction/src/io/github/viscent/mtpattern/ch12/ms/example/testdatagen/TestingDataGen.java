/*
授权声明：
本源码系《Java多线程编程实战指南（设计模式篇）》一书（ISBN：978-7-121-27006-2，以下称之为“原书”）的配套源码，
欲了解本代码的更多细节，请参考原书。
本代码仅为原书的配套说明之用，并不附带任何承诺（如质量保证和收益）。
以任何形式将本代码之部分或者全部用于营利性用途需经版权人书面同意。
将本代码之部分或者全部用于非营利性用途需要在代码中保留本声明。
任何对本代码的修改需在代码中以注释的形式注明修改人、修改时间以及修改内容。
本代码可以从以下网址下载：
https://github.com/Viscent/javamtp
http://www.broadview.com.cn/27006
 */

package io.github.viscent.mtpattern.ch12.ms.example.testdatagen;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 生成本章实战案例运行时所需的接口日志文件。
 * 
 * @author Viscent Huang
 *
 */
public class TestingDataGen {

	private static final String TIME_STAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat SDF_LONG;
	private final Logger logger = new Logger();

	static {
		SimpleTimeZone stz = new SimpleTimeZone(0, "UTC");
		SDF_LONG = new SimpleDateFormat(TIME_STAMP_FORMAT);
		SDF_LONG.setTimeZone(stz);
	}

	public static void main(String[] args) {
		TestingDataGen me = new TestingDataGen();
		Map<String, Map<String, Integer>> delayConf = createDefaultConf();
		int argc = args.length;
		int duration;// 模拟系统流量的时间跨度（发起请求到停止发起请求的持续时间）
		int overrallTPS;// 模拟系统的整体TPS（Transaction per Second）
		overrallTPS = (argc >= 1) ? Integer.valueOf(args[0]) : 100 * 3;
		duration = (argc >= 2) ? Integer.valueOf(args[1]) : 60;
		// 持续时间最短为1分钟（因为日志文件的文件名就是精确到分钟）
		me.generate(overrallTPS, duration, delayConf);

	}

	private static Map<String, Map<String, Integer>> createDefaultConf() {
		Map<String, Map<String, Integer>> delayConf = new HashMap<String, Map<String, Integer>>();

		Map<String, Integer> smsConf = new HashMap<String, Integer>();
		smsConf.put("NIG", 150);
		smsConf.put("ESB", 50);
		delayConf.put("Sms", smsConf);

		Map<String, Integer> locationConf = new HashMap<String, Integer>();
		locationConf.put("NIG", 500);
		locationConf.put("ESB", 20);
		delayConf.put("Location", locationConf);

		Map<String, Integer> chargingConf = new HashMap<String, Integer>();
		chargingConf.put("BSS", 1200);
		chargingConf.put("ESB", 50);
		delayConf.put("Charging", chargingConf);
		return delayConf;
	}

	public void generate(int overallTPS, int duration/* minutes */,
	    Map<String, Map<String, Integer>> respDelayConf) {
		if (duration < 1) {
			throw new IllegalArgumentException("duration must be greater than 1!");
		}

		doGenerate(overallTPS, duration, respDelayConf);
	}

	public void doGenerate(int overallTPS, int duration/* minutes */,
	    Map<String, Map<String, Integer>> respDelayConf) {

		int count = 0;
		RequestFactory rf;
		SimulatedRequest req;
		int factoryIndex = 0;
		Random rnd = new Random();

		long requestCount = overallTPS * (duration * 60);

		final int maxRequestPerTimeslice = (overallTPS / 3) * 10;
		final RequestFactory[] factories = new RequestFactory[] {
		    new SmsRequestFactory(respDelayConf.get("Sms"), maxRequestPerTimeslice),
		    new ChargingRequestFactory(respDelayConf.get("Charging"),
		        maxRequestPerTimeslice),
		    new LocationRequestFactory(respDelayConf.get("Location"),
		        maxRequestPerTimeslice) };

		Map<String, AtomicLong> requestCounter = new HashMap<String, AtomicLong>();
		AtomicLong reqCount;
		for (int i = 0; i < requestCount; i++) {

			factoryIndex = rnd.nextInt(factories.length);
			rf = factories[factoryIndex];

			req = rf.newRequest();

			req.printLogs(logger);

			reqCount = requestCounter.get(req.getInterfaceName());
			if (null == reqCount) {
				reqCount = new AtomicLong(0);
				requestCounter.put(req.getInterfaceName(), reqCount);
			}

			reqCount.incrementAndGet();

			count++;
		}

		System.out.println("Total request count:" + count);
		System.out.println(requestCounter);
	}

}
