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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import io.github.viscent.util.Tools;

/**
 * 运行以下类可以生成该程序所需的数据库记录：
 * io.github.viscent.mtpattern.ch13.pipeline.example.RecordGenerator
 * 
 * @author Viscent Huang
 *
 */
public class CaseRunner {

    public static void main(String[] args) throws IOException {
        Properties config = Tools.loadProperties(
                CaseRunner.class.getPackage().getName().replaceAll("\\.", "/")
                        + "/conf.properties");

        DataSyncTask dst;
        // dst = new DataSyncTask(config);
        // 运行本程序前，请根据实际情况修改以下方法中有关数据库连接和FTP账户的信息
        dst = new DataSyncTask(config) {
            {
                System.setProperty("ftp.client.impl",
                        "io.github.viscent.mtpattern.ch6.promise.example.FakeFTPUploader");
            }
            @Override
            protected RecordSource makeRecordSource(Properties config)
                    throws Exception {
                return new FakeRecordSource();
            }

        };

        dst.run();

    }

    // 模拟从数据库中读取数据
    private static class FakeRecordSource implements RecordSource {
        private final Scanner scanner;
        private final GZIPInputStream gis;
        public FakeRecordSource() throws IOException {
            this.gis = new GZIPInputStream(
                    new BufferedInputStream(CaseRunner.class
                            .getResourceAsStream(
                                    "/data/ch13/subscriptions.csv.gz")));
            this.scanner = new Scanner(gis, "UTF-8");
        }

        @Override
        public void close() throws IOException {
            try {
                scanner.close();
            } finally {
                gis.close();
            }
        }

        @Override
        public boolean hasNext() {
            return scanner.hasNextLine();
        }

        @Override
        public Record next() {
            String line = scanner.nextLine();
            Record record = null;
            try {
                record = Record.parseCsv(line);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return record;
        }
    }

}
