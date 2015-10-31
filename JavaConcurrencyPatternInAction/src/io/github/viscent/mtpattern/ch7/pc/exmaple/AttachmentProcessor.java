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

package io.github.viscent.mtpattern.ch7.pc.exmaple;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;
import io.github.viscent.mtpattern.ch7.pc.BlockingQueueChannel;
import io.github.viscent.mtpattern.ch7.pc.Channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

//模式角色：Producer-Consumer.Producer
public class AttachmentProcessor {
	private final String ATTACHMENT_STORE_BASE_DIR = "/home/viscent/tmp/attachments/";
	
	// 模式角色：Producer-Consumer.Channel
	private final Channel<File> channel = new BlockingQueueChannel<File>(
	    new ArrayBlockingQueue<File>(200));

	// 模式角色：Producer-Consumer.Consumer
	private final AbstractTerminatableThread indexingThread = new AbstractTerminatableThread() {

		@Override
		protected void doRun() throws Exception {
			File file = null;

			file = channel.take();
			try {
				indexFile(file);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				terminationToken.reservations.decrementAndGet();
			}

		}

		// 根据指定文件生成全文搜索所需的索引文件
		private void indexFile(File file) throws Exception {
			// 省略其它代码

			// 模拟生成索引文件的时间消耗
			Random rnd = new Random();
			try {
				Thread.sleep(rnd.nextInt(100));
			} catch (InterruptedException e) {
				;
			}
		}

	};

	public void init() {
		indexingThread.start();
	}

	public void shutdown() {
		indexingThread.terminate();
	}

	public void saveAttachment(InputStream in, String documentId,
	    String originalFileName) throws IOException {
		File file = saveAsFile(in, documentId, originalFileName);
		try {
			channel.put(file);
			indexingThread.terminationToken.reservations.incrementAndGet();
		} catch (InterruptedException e) {
			;
		}
	}

	private File saveAsFile(InputStream in, String documentId,
	    String originalFileName) throws IOException {
		String dirName = ATTACHMENT_STORE_BASE_DIR + documentId;
		File dir = new File(dirName);
		dir.mkdirs();
		File file = new File(dirName + '/'
		    + Normalizer.normalize(originalFileName, Normalizer.Form.NFC));

		// 防止目录跨越攻击
		if (!dirName.equals(file.getCanonicalFile().getParent())) {
			throw new SecurityException("Invalid originalFileName:"
			    + originalFileName);
		}

		BufferedOutputStream bos = null;
		BufferedInputStream bis = new BufferedInputStream(in);
		byte[] buf = new byte[2048];
		int len = -1;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			while ((len = bis.read(buf)) > 0) {
				bos.write(buf, 0, len);
			}
			bos.flush();
		} finally {
			try {
				bis.close();
			} catch (IOException e) {
				;
			}
			try {
				if (null != bos) {
					bos.close();
				}
			} catch (IOException e) {
				;
			}
		}

		return file;
	}

}
