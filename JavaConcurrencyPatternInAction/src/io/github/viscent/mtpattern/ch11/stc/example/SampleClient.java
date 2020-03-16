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

package io.github.viscent.mtpattern.ch11.stc.example;

import io.github.viscent.util.Tools;

public class SampleClient {
    private static final MessageFileDownloader DOWNLOADER;

    static {

        // 请根据实际情况修改构造器MessageFileDownloader的参数
        MessageFileDownloader mfd = null;
        try {
            mfd = new MessageFileDownloader(
                    Tools.getWorkingDir("ch11"),
                    "192.168.1.105", "ftp", "ftp", "~/messages");
            mfd.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DOWNLOADER = mfd;
    }

    public static void main(String[] args) {
        DOWNLOADER.downloadFile("abc.xml");
        DOWNLOADER.downloadFile("123.xml");
        DOWNLOADER.downloadFile("xyz.xml");

        // 执行其他操作
        Tools.randomPause(80000, 40000);
        DOWNLOADER.shutdown();
    }

}