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

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.github.viscent.util.Debug;
import io.github.viscent.util.Tools;

public class DataSyncTask implements Runnable {
    private final Map<String, String> taskParameters;

    public DataSyncTask(Map<String, String> taskParameters) {
        this.taskParameters = taskParameters;
    }

    @Override
    public void run() {
        String ftpServer = taskParameters.get("server");
        String ftpUserName = taskParameters.get("userName");
        String password = taskParameters.get("password");
        String serverDir = taskParameters.get("serverDir");

        /*
         * 接口FTPUploader用于对FTP客户端进行抽象，其源码参见本书配套下载。
         */
        // 初始化FTP客户端实例
        Future<FTPUploader> ftpClientUtilPromise =
                FTPUploaderPromisor.newFTPUploaderPromise(ftpServer,
                        ftpUserName, password, serverDir);

        // 查询数据库生成本地文件
        generateFilesFromDB();

        FTPUploader ftpClientUtil = null;
        try {
            // 获取初始化完毕的FTP客户端实例
            ftpClientUtil = ftpClientUtilPromise.get();
        } catch (InterruptedException e) {
            ;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // 上传文件
        uploadFiles(ftpClientUtil);

        // 省略其他代码
    }

    private void generateFilesFromDB() {
        Debug.info("generating files from database...");

        // 模拟实际操作所需的耗时
        Tools.randomPause(1000, 500);

        // 省略其他代码
    }

    private void uploadFiles(FTPUploader ftpClientUtil) {
        Set<File> files = retrieveGeneratedFiles();
        for (File file : files) {
            try {
                ftpClientUtil.upload(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected Set<File> retrieveGeneratedFiles() {
        Set<File> files = new HashSet<File>();

        // 模拟生成本地文件
        File currDir =
                new File(Tools.getWorkingDir(
                        "../classes/io/github/viscent/mtpattern/ch6/promise/example"));
        for (File f : currDir.listFiles(
                (dir, name) -> new File(dir, name).isFile()
                        && name.endsWith(".class"))) {
            files.add(f);
        }

        // 省略其他代码
        return files;
    }

}
