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

package io.github.viscent.mtpattern.ch12.ms.example;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.viscent.util.Tools;

/**
 * 
 * @author Viscent Huang
 *
 */
public class CaseRunner {

    public static void main(String[] args) throws Exception {
        String strLogFileBaseDir = System.getProperty("java.io.tmpdir")
                + "/tps/";

        // 解压缩程序所需的测试数据
        InputStream dataIn =
                CaseRunner.class.getClassLoader()
                        .getResourceAsStream("data/ch12/ifl.zip");
        Tools.unzip(dataIn, strLogFileBaseDir);

        final Pattern pattern;
        String matchingRegExp;

        // 用于选择要进行统计的接口日志文件的正则表达式，请根据实际情况修改。
        // ESB_interface_20180701
        // 2018070113
        // 20150420131
        // matchingRegExp = "20150420131[0-9]";
        matchingRegExp = "201807011[0-9]*";
        // matchingRegExp = "201807011[2-4]*";

        pattern = Pattern.compile("ESB_interface_" + matchingRegExp + ".log");
        PipedInputStream pipeIn = new PipedInputStream();
        final PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);

        // 创建并启动工作者线程，用于输出待统计的日志文件的文件名列表
        Thread thread = new Thread(() -> {
            new File(strLogFileBaseDir).list((dir, name) -> {
                Matcher matcher = pattern.matcher(name);
                boolean toAccept = matcher.matches();
                if (toAccept) {
                    try {
                        // 向TPSStat输出待统计的接口日志文件名
                        pipeOut.write((name + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return toAccept;
            });

            try {
                pipeOut.flush();
                pipeOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        /*
         * 将待统计的日志文件的文件列表通过输入流的形式传递给统计程序TPSStat， 这相当于在Linux下使用管道符“|”所产生的效果。
         */
        System.setIn(pipeIn);
        TPSStat.main(new String[] { strLogFileBaseDir });
    }

}