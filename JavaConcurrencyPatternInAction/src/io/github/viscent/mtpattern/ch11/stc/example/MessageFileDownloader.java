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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * 实现FTP文件下载。
 * 模式角色：SerialThreadConfinement.Serializer
 */
public class MessageFileDownloader {

    // 模式角色：SerialThreadConfinement.WorkerThread
    private final WorkerThread workerThread;

    public MessageFileDownloader(String outputDir, final String ftpServer,
            final String userName, final String password,
            final String servWorkingDir) throws Exception {
        Path path = Paths.get(outputDir);
        if (!path.toFile().exists()) {
            Files.createDirectories(path);
        }
        // workerThread = new WorkerThread(outputDir, ftpServer, userName,
        // password, servWorkingDir);
        workerThread = new FakeWorkerThread(outputDir, ftpServer, userName,
                password, servWorkingDir);
    }

    public void init() {
        workerThread.start();
    }

    public void shutdown() {
        workerThread.terminate();
    }

    public void downloadFile(String file) {
        workerThread.download(file);
    }
}