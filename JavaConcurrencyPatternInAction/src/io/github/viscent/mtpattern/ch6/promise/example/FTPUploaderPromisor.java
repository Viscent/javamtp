package io.github.viscent.mtpattern.ch6.promise.example;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

// 模式角色：Promise.Promisor
public class FTPUploaderPromisor {

    // 模式角色：Promise.Promisor.compute
    public static Future<FTPUploader> newFTPUploaderPromise(String ftpServer,
            String ftpUserName, String password, String serverDir) {
        Executor helperExecutor=new Executor() {
            @Override
            public void execute(Runnable command) {
                Thread t = new Thread(command);
                t.start();
            }
        };
        return newFTPUploaderPromise(
                ftpServer, ftpUserName, password, serverDir, helperExecutor);
    }

    // 模式角色：Promise.Promisor.compute
    public static Future<FTPUploader> newFTPUploaderPromise(String ftpServer,
            String ftpUserName, String password, String serverDir,
            Executor helperExecutor) {
        Callable<FTPUploader> callable = new Callable<FTPUploader>() {

            @Override
            public FTPUploader call() throws Exception {
                String implClazz = System.getProperty("ftp.client.impl");
                // 类FTPClientUtil的源码参见本书配套下载
                if (null == implClazz) {
                    implClazz =
                            "io.github.viscent.mtpattern.ch6.promise.example"
                                    + ".FTPClientUtil";
                }
                FTPUploader ftpUploader;
                ftpUploader = (FTPUploader) Class.forName(implClazz).newInstance();
                ftpUploader.init(ftpServer, ftpUserName, password, serverDir);
                return ftpUploader;
            }

        };

        // task相当于模式角色：Promise.Promise
        final FutureTask<FTPUploader> task = new FutureTask<>(callable);
        helperExecutor.execute(task);
        return task;
    }

}
