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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

//模式角色：Promise.Promisor、Promise.Result
public class FTPClientUtil {
	private final FTPClient ftp = new FTPClient();

	private final Map<String, Boolean> dirCreateMap = new HashMap<String, Boolean>();
	/*
	 * helperExecutor是个静态变量，这使得newInstance方法在生成不同的FTPClientUtil实例时可以共用同一个线程池。
	 * 模式角色：Promise.TaskExecutor
	 */
	private volatile static ExecutorService helperExecutor;

	static {

		helperExecutor = new ThreadPoolExecutor(1, Runtime.getRuntime()
		    .availableProcessors() * 2, 60, TimeUnit.SECONDS,
		    new ArrayBlockingQueue<Runnable>(10), new ThreadFactory() {
			    public Thread newThread(Runnable r) {
				    Thread t = new Thread(r);
				    t.setDaemon(true);
				    return t;
			    }
		    }, new ThreadPoolExecutor.CallerRunsPolicy());
	}

	// 私有构造器
	private FTPClientUtil() {

	}

	// 模式角色：Promise.Promisor.compute
	public static Future<FTPClientUtil> newInstance(final String ftpServer,
	    final String userName, final String password) {

		Callable<FTPClientUtil> callable = new Callable<FTPClientUtil>() {

			@Override
			public FTPClientUtil call() throws Exception {
				FTPClientUtil self = new FTPClientUtil();
				self.init(ftpServer, userName, password);
				return self;
			}

		};

		// task相当于模式角色：Promise.Promise
		final FutureTask<FTPClientUtil> task = new FutureTask<FTPClientUtil>(
		    callable);
		helperExecutor.execute(task);
		return task;
	}

	private void init(String ftpServer, String userName, String password)
	    throws Exception {

		FTPClientConfig config = new FTPClientConfig();
		ftp.configure(config);

		int reply;
		ftp.connect(ftpServer);

		System.out.print(ftp.getReplyString());

		reply = ftp.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			throw new RuntimeException("FTP server refused connection.");
		}
		boolean isOK = ftp.login(userName, password);
		if (isOK) {
			System.out.println(ftp.getReplyString());

		} else {
			throw new RuntimeException("Failed to login." + ftp.getReplyString());
		}

		reply = ftp.cwd("~/subspsync");
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			throw new RuntimeException("Failed to change working directory.reply:"
			    + reply);
		} else {

			System.out.println(ftp.getReplyString());
		}

		ftp.setFileType(FTP.ASCII_FILE_TYPE);

	}

	public void upload(File file) throws Exception {
		InputStream dataIn = new BufferedInputStream(new FileInputStream(file),
		    1024 * 8);
		boolean isOK;
		String dirName = file.getParentFile().getName();
		String fileName = dirName + '/' + file.getName();
		ByteArrayInputStream checkFileInputStream = new ByteArrayInputStream(
		    "".getBytes());

		try {
			if (!dirCreateMap.containsKey(dirName)) {
				ftp.makeDirectory(dirName);
				dirCreateMap.put(dirName, null);
			}

			try {
				isOK = ftp.storeFile(fileName, dataIn);
			} catch (IOException e) {
				throw new RuntimeException("Failed to upload " + file, e);
			}
			if (isOK) {
				ftp.storeFile(fileName + ".c", checkFileInputStream);

			} else {

				throw new RuntimeException("Failed to upload " + file + ",reply:" + ","
				    + ftp.getReplyString());
			}
		} finally {
			dataIn.close();
		}

	}

	public void disconnect() {
		if (ftp.isConnected()) {
			try {
				ftp.disconnect();
			} catch (IOException ioe) {
				// 什么也不做
			}
		}
	}
}
