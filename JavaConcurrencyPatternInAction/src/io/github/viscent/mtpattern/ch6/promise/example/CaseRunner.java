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

package io.github.viscent.mtpattern.ch6.promise.example;

import java.util.HashMap;
import java.util.Map;

public class CaseRunner {

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<String, String>();

        // FTP服务器IP地址，运行代码时请根据实际情况修改！
        params.put("server", "10.0.0.5");

        // FTP账户名，运行代码时请根据实际情况修改！
        params.put("userName", "datacenter");

        // FTP账户密码，运行代码时请根据实际情况修改！
        params.put("password", "abc123");

        // FTP服务器基准目录！
        params.put("serverDir", "~/subspsync");

        // 如果要使用真实的FTP服务器，请将下面的这条语句注释掉
        System.setProperty("ftp.client.impl",
                "io.github.viscent.mtpattern.ch6.promise.example.FakeFTPUploader");

        DataSyncTask dst;
        dst = new DataSyncTask(params);
        Thread t = new Thread(dst);
        t.start();

    }
}