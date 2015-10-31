/*
授权声明：
本源码系《Java多线程编程实战指南（设计模式篇）》一书（ISBN：978-1-7-121-27006-2，以下称之为“原书”）的配套源码，
欲了解本代码的更多细节，请参考原书。
本代码仅为原书的配套说明之用，并不附带任何承诺（如质量保证和收益）。
以任何形式将本代码之部分或者全部用于营利性用途需经版权人书面同意。
将本代码之部分或者全部用于非营利性用途需要在代码中保留本声明。
任何对本代码的修改需在代码中以注释的形式注明修改人、修改时间以及修改内容。
本代码可以从下URL下载：
https://github.com/Viscent/javamtp
http://www.broadview.com.cn/27006
*/

package io.github.viscent.mtpattern.ch11.stc.example;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

/*
 * 实现FTP文件下载。
 * 模式角色：SerialThreadConfiment.Serializer
 */
public class MessageFileDownloader {

	// 模式角色：SerialThreadConfiment.WorkerThread
	private final WorkerThread workerThread;

	public MessageFileDownloader(String outputDir, final String ftpServer,
	    final String userName, final String password) {

		workerThread = new WorkerThread(outputDir, ftpServer, userName, password);

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

	// 模式角色：SerialThreadConfiment.WorkerThread
	private static class WorkerThread extends AbstractTerminatableThread {

		// 模式角色：SerialThreadConfiment.Queue
		private final BlockingQueue<String> workQueue;
		private final Future<FTPClient> ftpClientPromise;
		private final String outputDir;

		public WorkerThread(String outputDir, final String ftpServer,
		    final String userName, final String password) {
			this.workQueue = new ArrayBlockingQueue<String>(100);
			this.outputDir = outputDir + '/';

			this.ftpClientPromise = new FutureTask<FTPClient>(
			    new Callable<FTPClient>() {

				    @Override
				    public FTPClient call() throws Exception {
					    FTPClient ftpClient = initFTPClient(ftpServer, userName, password);
					    return ftpClient;
				    }

			    });

			new Thread((FutureTask<FTPClient>) ftpClientPromise).start();
		}

		public void download(String file) {
			try {
				workQueue.put(file);
				terminationToken.reservations.incrementAndGet();
			} catch (InterruptedException e) {
				;
			}
		}

		private FTPClient initFTPClient(String ftpServer, String userName,
		    String password) throws Exception {
			FTPClient ftpClient = new FTPClient();

			FTPClientConfig config = new FTPClientConfig();
			ftpClient.configure(config);

			int reply;
			ftpClient.connect(ftpServer);

			System.out.print(ftpClient.getReplyString());

			reply = ftpClient.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				throw new RuntimeException("FTP server refused connection.");
			}

			boolean isOK = ftpClient.login(userName, password);
			if (isOK) {
				System.out.println(ftpClient.getReplyString());

			} else {
				throw new RuntimeException("Failed to login."
				    + ftpClient.getReplyString());
			}

			reply = ftpClient.cwd("~/messages");
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				throw new RuntimeException("Failed to change working directory.reply:"
				    + reply);
			} else {

				System.out.println(ftpClient.getReplyString());
			}

			ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
			return ftpClient;

		}

		@Override
		protected void doRun() throws Exception {
			String file = workQueue.take();

			OutputStream os = null;
			try {
				os = new BufferedOutputStream(new FileOutputStream(outputDir + file));
				ftpClientPromise.get().retrieveFile(file, os);
			} finally {
				if (null != os) {
					try {
						os.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				terminationToken.reservations.decrementAndGet();
			}

		}

		@Override
		protected void doCleanup(Exception cause) {
			try {
				ftpClientPromise.get().disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

	}
}