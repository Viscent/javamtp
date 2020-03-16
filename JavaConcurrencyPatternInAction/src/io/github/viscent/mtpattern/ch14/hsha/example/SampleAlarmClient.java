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

import io.github.viscent.mtpattern.ch5.tpt.example.AlarmType;

import java.sql.Connection;

import org.apache.log4j.Logger;

public class SampleAlarmClient {
	private final static Logger logger = Logger
	    .getLogger(SampleAlarmClient.class);

	// 告警日志抑制阈值
	private static final int ALARM_MSG_SUPRESS_THRESHOLD = 10;

	static {

		// 初始化告警模块
		AlarmMgr.getInstance().init();
	}

	public static void main(String[] args) {
		SampleAlarmClient alarmClient = new SampleAlarmClient();
		Connection dbConn = null;
		try {
			dbConn = alarmClient.retrieveDBConnection();

		} catch (Exception e) {
			final AlarmMgr alarmMgr = AlarmMgr.getInstance();

			// 告警被重复发送至告警模块的次数
			int duplicateSubmissionCount;
			String alarmId = "00000010000020";
			final String alarmExtraInfo = "operation=GetDBConnection;detail=Failed to get DB connection:"
			    + e.getMessage();

			duplicateSubmissionCount = alarmMgr.sendAlarm(AlarmType.FAULT, alarmId,
			    alarmExtraInfo);
			if (duplicateSubmissionCount < ALARM_MSG_SUPRESS_THRESHOLD) {
				logger.error("Alarm[" + alarmId + "] raised,extraInfo:"
				    + alarmExtraInfo);
			} else {
				if (duplicateSubmissionCount == ALARM_MSG_SUPRESS_THRESHOLD) {
					logger.error("Alarm[" + alarmId + "] was raised more than "
					    + ALARM_MSG_SUPRESS_THRESHOLD
					    + " times, it will no longer be logged.");
				}
			}
			return;
		}

		alarmClient.doSomething(dbConn);

	}

	// 获取数据库连接
	private Connection retrieveDBConnection() throws Exception {
		Connection cnn = null;

		// 省略其他代码

		return cnn;
	}

	private void doSomething(Connection conn) {
		// 省略其他代码
	}

}
