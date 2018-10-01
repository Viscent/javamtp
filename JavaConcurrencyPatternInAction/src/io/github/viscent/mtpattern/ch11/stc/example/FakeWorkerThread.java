package io.github.viscent.mtpattern.ch11.stc.example;

import org.apache.commons.net.ftp.FTPClient;

import io.github.viscent.util.Debug;
import io.github.viscent.util.Tools;

public class FakeWorkerThread extends WorkerThread {

    public FakeWorkerThread(String outputDir, String ftpServer, String userName,
            String password, String servWorkingDir) throws Exception {
        super(outputDir, ftpServer, userName, password, servWorkingDir);
    }

    @Override
    protected FTPClient initFTPClient(String ftpServer, String userName,
            String password) throws Exception {
        FTPClient ftpClient = new FTPClient();
        return ftpClient;
    }

    @Override
    protected void doRun() throws Exception {
        String file = workQueue.take();
        try {
            Debug.info("Download file %s", file);
            Tools.randomPause(80, 50);
        } finally {
            terminationToken.reservations.decrementAndGet();
        }

    }

}
