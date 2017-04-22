/*
授权声明：
本源码系《Java多线程编程实战指南（设计模式篇）》一书（ISBN：978-7-121-27006-2，以下称之为“原书”）的配套源码，
欲了解本代码的更多细节，请参考原书。
本代码仅为原书的配套说明之用，并不附带任何承诺（如质量保证和收益）。
以任何形式将本代码之部分或者全部用于营利性用途需经版权人书面同意。
将本代码之部分或者全部用于非营利性用途需要在代码中保留本声明。
任何对本代码的修改需在代码中以注释的形式注明修改人、修改时间以及修改内容。
本代码可以从以下网址下载：
https://github.com/Viscent/javamtp
http://www.broadview.com.cn/27006
*/

package io.github.viscent.mtpattern.ch13.pipeline.example;

import io.github.viscent.mtpattern.ch13.pipeline.AbstractParallelPipe;
import io.github.viscent.mtpattern.ch13.pipeline.AbstractPipe;
import io.github.viscent.mtpattern.ch13.pipeline.Pipe;
import io.github.viscent.mtpattern.ch13.pipeline.PipeContext;
import io.github.viscent.mtpattern.ch13.pipeline.PipeException;
import io.github.viscent.mtpattern.ch13.pipeline.Pipeline;
import io.github.viscent.mtpattern.ch13.pipeline.SimplePipeline;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DataSyncTask implements Runnable {

	public void run() {
		ResultSet rs = null;
		SimplePipeline<RecordSaveTask, String> pipeline = buildPipeline();
		pipeline.init(pipeline.newDefaultPipelineContext());

		Connection dbConn = null;
		try {
			dbConn = getConnection();
			rs = qryRecords(dbConn);

			processRecords(rs, pipeline);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != dbConn) {
				try {
					dbConn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		pipeline.shutdown(360, TimeUnit.SECONDS);
	}

	private ResultSet qryRecords(Connection dbConn) throws Exception {

		dbConn.setReadOnly(true);
		PreparedStatement ps = dbConn
		    .prepareStatement(
		        "select id,productId,packageId,msisdn,operationTime,operationType,"
		            + "effectiveDate,dueDate from subscriptions order by operationTime",
		        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = ps.executeQuery();
		return rs;
	}

	protected Connection getConnection() throws Exception {
		Connection dbConn = null;

		Class.forName("org.hsqldb.jdbc.JDBCDriver");

		dbConn = DriverManager.getConnection(
		    "jdbc:hsqldb:hsql://192.168.1.105:9001/viscent-test", "SA", "");
		return dbConn;
	}

	private static Record makeRecordFrom(ResultSet rs) throws SQLException {
		Record record = new Record();

		record.setId(rs.getInt("id"));
		record.setProductId(rs.getString("productId"));
		record.setPackageId(rs.getString("packageId"));
		record.setMsisdn(rs.getString("msisdn"));
		record.setOperationTime(rs.getTimestamp("operationTime"));
		record.setOperationType(rs.getInt("operationType"));
		record.setEffectiveDate(rs.getTimestamp("effectiveDate"));
		record.setDueDate(rs.getTimestamp("dueDate"));
		return record;
	}

	private static class RecordSaveTask {
		public final Record[] records;
		public final int targetFileIndex;
		public final String recordDay;

		public RecordSaveTask(Record[] records, int targetFileIndex) {
			this.records = records;
			this.targetFileIndex = targetFileIndex;
			this.recordDay = null;
		}

		public RecordSaveTask(String recordDay, int targetFileIndex) {
			this.records = null;
			this.targetFileIndex = targetFileIndex;
			this.recordDay = recordDay;
		}

	}

	@SuppressWarnings("unchecked")
	private SimplePipeline<RecordSaveTask, String> buildPipeline() {
		/*
		 * 线程池的本质是重复利用一定数量的线程，而不是针对每个任务都有一个专门的工作者线程。
		 * 这里，各个Pipe的初始化话完全可以在上游Pipe初始化完毕后再初始化化其后继Pipe，而不必多个Pipe同时初始化。
		 * 因此，这个初始化的动作可以由一个线程来处理。该线程处理完各个Pipe的初始化后，可以继续处理之后可能产生的任务， 如错误处理。
		 * 所以，上述这些先后产生的任务可以由线程池中的一个工作者线程从头到尾负责执行。
		 */
		final ExecutorService helperExecutor = Executors.newSingleThreadExecutor();

		final SimplePipeline<RecordSaveTask, String> pipeline = new SimplePipeline<RecordSaveTask, String>(
		    helperExecutor);

		// 根据数据库记录生成相应的数据文件
		Pipe<RecordSaveTask, File> stageSaveFile = new AbstractPipe<RecordSaveTask, File>() {

			@Override
			protected File doProcess(RecordSaveTask task) throws PipeException {
				final RecordWriter recordWriter = RecordWriter.getInstance();
				final Record[] records = task.records;
				File file;
				if (null == records) {
					file = recordWriter.finishRecords(task.recordDay,
					    task.targetFileIndex);
				} else {
					try {
						file = recordWriter.write(records, task.targetFileIndex);
					} catch (IOException e) {
						throw new PipeException(this, task, "Failed to save records.", e);
					}
				}
				return file;
			}
		};

		/*
		 * 由于这里的几个Pipe都是处理I/O的，为了避免使用锁（以减少不必要的上下文切换） 但又能保证线程安全，故每个Pipe都采用单线程处理。
		 * 若各个Pipe要改用线程池来处理，需要注意：1）线程安全 2）死锁
		 */
		pipeline.addAsWorkerThreadBasedPipe(stageSaveFile, 1);

		final String[][] ftpServerConfigs = retrieveFTPServConf();

		final ThreadPoolExecutor ftpExecutorService = new ThreadPoolExecutor(1,
		    ftpServerConfigs.length, 60, TimeUnit.SECONDS,
		    new ArrayBlockingQueue<Runnable>(100), new RejectedExecutionHandler() {
			    @Override
			    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				    if (!executor.isShutdown()) {
					    try {
						    executor.getQueue().put(r);
					    } catch (InterruptedException e) {
						    ;
					    }
				    }
			    }

		    });

		// 将生成的数据文件传输到指定的主机上。
		Pipe<File, File> stageTransferFile = new AbstractParallelPipe<File, File, File>(
		    new SynchronousQueue<File>(), ftpExecutorService) {
			final Future<FTPClientUtil>[] ftpClientUtilHolders = new Future[ftpServerConfigs.length];

			@Override
			public void init(PipeContext pipeCtx) {
				super.init(pipeCtx);

				String[] ftpServerConfig;
				for (int i = 0; i < ftpServerConfigs.length; i++) {
					ftpServerConfig = ftpServerConfigs[i];
					ftpClientUtilHolders[i] = FTPClientUtil.newInstance(
					    ftpServerConfig[0], ftpServerConfig[1], ftpServerConfig[2]);
				}

			}

			@Override
			protected List<Callable<File>> buildTasks(final File file) {
				List<Callable<File>> tasks = new LinkedList<Callable<File>>();

				for (Future<FTPClientUtil> ftpClientUtilHolder : ftpClientUtilHolders) {
					tasks.add(new ParallelTask(ftpClientUtilHolder, file));
				}

				return tasks;
			}

			@Override
			protected File combineResults(List<Future<File>> subTaskResults)
			    throws Exception {
				if (0 == subTaskResults.size()) {
					return null;
				}
				File file = null;

				file = subTaskResults.get(0).get();

				return file;
			}

			@Override
			public void shutdown(long timeout, TimeUnit unit) {
				super.shutdown(timeout, unit);

				ftpExecutorService.shutdown();

				try {
					ftpExecutorService.awaitTermination(timeout, unit);
				} catch (InterruptedException e1) {
					;
				}
				for (Future<FTPClientUtil> ftpClientUtilHolder : ftpClientUtilHolders) {
					try {
						ftpClientUtilHolder.get().disconnect();
					} catch (Exception e) {
						;
					}
				}
			}

			class ParallelTask implements Callable<File> {
				public final Future<FTPClientUtil> ftpUtilHodler;
				public final File file2Transfer;

				public ParallelTask(Future<FTPClientUtil> ftpUtilHodler,
				    File file2Transfer) {
					this.ftpUtilHodler = ftpUtilHodler;
					this.file2Transfer = file2Transfer;
				}

				@Override
				public File call() throws Exception {
					File transferedFile = null;
					ftpUtilHodler.get().upload(file2Transfer);
					transferedFile = file2Transfer;
					return transferedFile;
				}

			}

		};

		pipeline.addAsWorkerThreadBasedPipe(stageTransferFile, 1);

		// 备份已经传输的数据文件
		Pipe<File, Void> stageBackupFile = new AbstractPipe<File, Void>() {
			@Override
			protected Void doProcess(File transferedFile) throws PipeException {
				RecordWriter.backupFile(transferedFile);
				return null;
			}

			@Override
			public void shutdown(long timeout, TimeUnit unit) {

				// 所有文件备份完毕后，清理掉空文件夹
				RecordWriter.purgeDir();
			}

		};
		
		pipeline.addAsWorkerThreadBasedPipe(stageBackupFile,1);

		return pipeline;
	}

	protected String[][] retrieveFTPServConf() {
		String[][] ftpServerConfigs = new String[][] {
		    { "192.168.1.105", "datacenter", "abc123" }
//		    ,
//		    { "192.168.1.104", "datacenter", "abc123" }
//		    ,
//		    { "192.168.1.103", "datacenter", "abc123" } 
		    };
		return ftpServerConfigs;
	}

	private void processRecords(ResultSet rs,
	    Pipeline<RecordSaveTask, String> pipeline) throws Exception {
		Record record;
		Record[] records = new Record[Config.RECORD_SAVE_CHUNK_SIZE];
		int targetFileIndex = 0;
		int nextTargetFileIndex = 0;
		int recordCountInTheDay = 0;
		int recordCountInTheFile = 0;
		String recordDay = null;
		String lastRecordDay = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

		while (rs.next()) {
			record = makeRecordFrom(rs);

			lastRecordDay = recordDay;

			recordDay = sdf.format(record.getOperationTime());

			if (recordDay.equals(lastRecordDay)) {
				records[recordCountInTheFile] = record;
				recordCountInTheDay++;
			} else {
				// 实际已发生的不同日期记录文件切换
				if (null != lastRecordDay) {
					if (recordCountInTheFile >= 1) {
						pipeline.process(new RecordSaveTask(Arrays.copyOf(records,
						    recordCountInTheFile), targetFileIndex));
					} else {
						pipeline
						    .process(new RecordSaveTask(lastRecordDay, targetFileIndex));
					}

					// 在此之前，先将records中的内容写入文件
					records[0] = record;
					recordCountInTheFile = 0;
				} else {
					// 直接赋值
					records[0] = record;
				}

				recordCountInTheDay = 1;
			}

			if (nextTargetFileIndex == targetFileIndex) {
				recordCountInTheFile++;
				if (0 == (recordCountInTheFile % Config.RECORD_SAVE_CHUNK_SIZE)) {
					pipeline.process(new RecordSaveTask(Arrays.copyOf(records,
					    recordCountInTheFile), targetFileIndex));
					recordCountInTheFile = 0;
				}
			}

			nextTargetFileIndex = (recordCountInTheDay) / Config.MAX_RECORDS_PER_FILE;
			if (nextTargetFileIndex > targetFileIndex) {
				// 预测到将发生同日期记录文件切换
				if (recordCountInTheFile > 1) {
					pipeline.process(new RecordSaveTask(Arrays.copyOf(records,
					    recordCountInTheFile), targetFileIndex));
				} else {
					pipeline.process(new RecordSaveTask(recordDay, targetFileIndex));
				}

				recordCountInTheFile = 0;

				targetFileIndex = nextTargetFileIndex;
			} else if (nextTargetFileIndex < targetFileIndex) {
				// 实际已发生的异日期记录文件切换,recordCountInTheFile保持当前值
				targetFileIndex = nextTargetFileIndex;
			}

		}

		if (recordCountInTheFile > 0) {
			pipeline.process(new RecordSaveTask(Arrays.copyOf(records,
			    recordCountInTheFile), targetFileIndex));
		}

	}

}