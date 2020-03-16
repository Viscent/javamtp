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

package io.github.viscent.mtpattern.ch13.pipeline.example;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Random;

import io.github.viscent.util.Tools;

/**
 * 本章实战案例运行时所需的数据库记录生成器。
 * 数据库的初始化可以使用数据库脚本DBScript.sql手工执行，也可以在执行本程序时在命令行中添加参数“init”。
 * 
 * @author Viscent Huang
 *
 */
public class RecordGenerator {


    public static void main(String[] args) throws Exception {
        Connection dbConn = getConnection();

        if (args.length > 0 && "init".equals(args[0])) {
            Statement st = dbConn.createStatement();
            URL scriptURL = RecordGenerator.class
                    .getResource(RecordGenerator.class.getPackage().getName()
                            .replaceAll("\\.", "/" + "DBScript.sql"));

            String script = new String(
                    Files.readAllBytes(Paths.get(scriptURL.toURI())));
            st.execute(script);
            st.close();
        }

        try {
            String[] msisdns = new String[] { "13612345678", "13712345678",
                    "13812345678", "15912345678" };
            String[] operationTimes = new String[] { "2014-08-08 20:08:08",
                    "2014-08-09 10:08:08", "2014-08-10 09:58:08",
                    "2014-08-10 12:58:08" };

            PreparedStatement ps = dbConn
                    .prepareStatement(
                            "Insert into subscriptions(id,productId,packageId,msisdn,operationTime,operationType,effectiveDate,dueDate) "
                                    + "values(?,?,'pkg0000001',?,?,0,'2014-09-01 00:00:00','2014-12-01 23:59:59')");
            DecimalFormat df = new DecimalFormat("0000");

            Random rnd = new Random();
            Random rnd1 = new Random();
            Random rnd2 = new Random();

            int count;

            if (args.length > 0) {
                count = Integer.valueOf(args[0]);
            } else {
                count = 10000;
            }

            for (int i = 0; i < count; i++) {
                ps.setInt(1, i);
                ps.setString(2, "p0000" + df.format(rnd1.nextInt(9999)));
                ps.setString(3, msisdns[rnd.nextInt(msisdns.length)]);
                ps.setString(4,
                        operationTimes[rnd2.nextInt(operationTimes.length)]);
                ps.addBatch();

            }

            ps.executeBatch();
        } finally {

            dbConn.close();
        }

    }

    static Connection getConnection() throws Exception {
        Connection dbConn = null;
        Properties props = Tools.loadProperties(
                RecordGenerator.class.getPackage().getName().replaceAll("\\.",
                        "/")
                        + "/conf.properties");
        Class.forName(props.getProperty("jdbc.driver"));

        dbConn = DriverManager.getConnection(props.getProperty("jdbc.url"),
                props.getProperty("jdbc.username"),
                props.getProperty("jdbc.password", ""));
        return dbConn;
    }

}
