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

package io.github.viscent.mtpattern.ch13.pipeline.example;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 运行以下类可以生成该程序所需的数据库记录：
 * io.github.viscent.mtpattern.ch13.pipeline.example.RecordGenerator
 * 
 * @author Viscent Huang
 *
 */
public class CaseRunner {

	public static void main(String[] args) {

		// 运行本程序前，请根据实际情况修改以下方法中有关数据库连接和FTP账户的信息
		DataSyncTask dst = new DataSyncTask() {

			@Override
			protected Connection getConnection() throws Exception {
				Connection dbConn = null;

				Class.forName("org.hsqldb.jdbc.JDBCDriver");

				dbConn = DriverManager.getConnection(
				    "jdbc:hsqldb:hsql://192.168.1.105:9001/viscent-test", "SA", "");
				return dbConn;
			}

			@Override
			protected String[][] retrieveFTPServConf() {
				String[][] ftpServerConfigs = new String[][] { { "192.168.1.105",
				    "datacenter", "abc123" }
				// ,
				// { "192.168.1.104", "datacenter", "abc123" }
				// ,
				// { "192.168.1.103", "datacenter", "abc123" }
				};
				
				return ftpServerConfigs;
			}

		};
		
		dst.run();
	}

}
