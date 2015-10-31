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

package io.github.viscent.mtpattern.ch6.promise.example;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

public class FTPClientUtil {
	
	private final FTPClient ftp = new FTPClient();
	private final Map<String, Boolean> dirCreateMap = new HashMap<String, Boolean>();

	// 私有构造器
	private FTPClientUtil() {

	}

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
		final FutureTask<FTPClientUtil> task = new FutureTask<FTPClientUtil>(
		    callable);

		// 下面这行代码与本案例的实际代码并不一致，这是为了讨论方便。
		new Thread(task).start();

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
		//省略与清单6-2中相同的代码
	}
}
