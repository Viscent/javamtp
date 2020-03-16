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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import io.github.viscent.mtpattern.ch5.tpt.AbstractTerminatableThread;
import io.github.viscent.util.Debug;

public class TPSStat {

    public static void main(String[] args) throws Exception {
        // 接口日志文件所在目录
        String logBaseDir = args[0];
        // 忽略的操作名列表
        String excludedOperationNames = "";
        // 指定要统计在内的操作名列表
        String includedOperationNames = "sendSms,";
        // 指定要统计在内的目标设备名
        String destinationSysName = "*";
        int argc = args.length;

        if (argc > 2) {
            excludedOperationNames = args[1];
        }
        if (argc > 3) {
            excludedOperationNames = args[2];
        }
        if (argc > 4) {
            destinationSysName = args[3];
        }
        Master processor = new Master(logBaseDir, excludedOperationNames,
                includedOperationNames, destinationSysName);
        BufferedReader fileNamesReader = new BufferedReader(
                new InputStreamReader(System.in));
        ConcurrentMap<String, AtomicInteger> result = processor
                .calculate(fileNamesReader);
        /*
         * 统计周期为10秒钟。 输出格式：“时间段,时间段内的请求总数”
         */
        for (String timeRange : result.keySet()) {
            System.out.println(timeRange + "," + result.get(timeRange));
        }
    }

    // 模式角色：Master-Slave.Master
    private static class Master {
        final String logFileBaseDir;
        final String excludedOperationNames;
        final String includedOperationNames;
        final String destinationSysName;

        // 每次派发给某个Slave线程的文件个数
        static final int NUMBER_OF_FILES_FOR_EACH_DISPATCH = 5;
        static final int WORKER_COUNT =
                Runtime.getRuntime()
                .availableProcessors();

        public Master(String logFileBaseDir, String excludedOperationNames,
                String includedOperationNames,
                String destinationSysName) {
            this.logFileBaseDir = logFileBaseDir;
            this.excludedOperationNames = excludedOperationNames;
            this.includedOperationNames = includedOperationNames;
            this.destinationSysName = destinationSysName;
        }

        public ConcurrentMap<String, AtomicInteger> calculate(
                BufferedReader fileNamesReader) throws IOException {
            ConcurrentMap<String, AtomicInteger> repository =
                    new ConcurrentSkipListMap<>();
            // 创建工作者线程
            Worker[] workers = createAndStartWorkers(repository);
            // 指派任务给工作者线程
            dispatchTask(fileNamesReader, workers);
            // 等待工作者线程处理结束
            for (int i = 0; i < WORKER_COUNT; i++) {
                workers[i].terminate(true);
            }
            // 返回处理结果
            return repository;
        }

        private Worker[] createAndStartWorkers(
                ConcurrentMap<String, AtomicInteger> repository) {
            Worker[] workers = new Worker[WORKER_COUNT];
            Worker worker;
            UncaughtExceptionHandler eh = new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            };

            for (int i = 0; i < WORKER_COUNT; i++) {
                worker = new Worker(repository, excludedOperationNames,
                        includedOperationNames, destinationSysName);
                workers[i] = worker;
                worker.setUncaughtExceptionHandler(eh);
                worker.start();
            }
            return workers;
        }

        private void dispatchTask(BufferedReader fileNamesReader,
                Worker[] workers) throws IOException {
            String line;
            Set<String> fileNames = new HashSet<String>();
            int fileCount = 0;
            int workerIndex = -1;
            BufferedReader logFileReader;
            while ((line = fileNamesReader.readLine()) != null) {
                fileNames.add(line);
                fileCount++;
                if (0 == (fileCount % NUMBER_OF_FILES_FOR_EACH_DISPATCH)) {
                    // 工作者线程间的负载均衡：采用简单的轮询选择worker
                    workerIndex = (workerIndex + 1) % WORKER_COUNT;
                    logFileReader = makeReaderFrom(fileNames);
                    Debug.info("Dispatch " + NUMBER_OF_FILES_FOR_EACH_DISPATCH
                            + " files to worker:" + workerIndex);
                    workers[workerIndex].submitWorkload(logFileReader);
                    fileNames = new HashSet<String>();
                    fileCount = 0;
                }
            }

            if (fileCount > 0) {
                logFileReader = makeReaderFrom(fileNames);
                workerIndex = (workerIndex + 1) % WORKER_COUNT;
                workers[workerIndex].submitWorkload(logFileReader);
            }
        }

        private BufferedReader makeReaderFrom(final Set<String> logFileNames) {
            BufferedReader logFileReader;

            InputStream in = new SequenceInputStream(
                    new Enumeration<InputStream>() {
                        private Iterator<String> iterator = logFileNames
                                .iterator();
                        @Override
                        public boolean hasMoreElements() {
                            return iterator.hasNext();
                        }

                        @Override
                        public InputStream nextElement() {
                            String fileName = iterator.next();
                            InputStream in = null;
                            try {
                                in = new FileInputStream(
                                        logFileBaseDir + fileName);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            return in;
                        }

                    });
            logFileReader = new BufferedReader(new InputStreamReader(in));
            return logFileReader;
        }// end of makeReaderFrom
    }

    // 模式角色：Master-Slave.Slave
    private static class Worker extends AbstractTerminatableThread {
        private static final Pattern SPLIT_PATTERN = Pattern.compile("\\|");
        private final ConcurrentMap<String, AtomicInteger> repository;
        private final BlockingQueue<BufferedReader> workQueue;
        private final String selfDevice = "ESB";
        private final String excludedOperationNames;
        private final String includedOperationNames;
        private final String destinationSysName;

        public Worker(ConcurrentMap<String, AtomicInteger> repository,
                String excludedOperationNames,
                String includedOperationNames, String destinationSysName) {
            this.repository = repository;
            workQueue = new ArrayBlockingQueue<BufferedReader>(100);
            this.excludedOperationNames = excludedOperationNames;
            this.includedOperationNames = includedOperationNames;
            this.destinationSysName = destinationSysName;
        }

        public void submitWorkload(BufferedReader taskWorkload) {
            try {
                workQueue.put(taskWorkload);
                terminationToken.reservations.incrementAndGet();
            } catch (InterruptedException e) {
                // 什么也不做
            }
        }

        @Override
        protected void doRun() throws Exception {
            BufferedReader logFileReader = workQueue.take();

            String interfaceLogRecord;
            String[] recordParts;
            String timeStamp;
            AtomicInteger reqCounter;
            AtomicInteger existingReqCounter;
            int i = 0;

            try {
                while ((interfaceLogRecord = logFileReader
                        .readLine()) != null) {
                    recordParts = SPLIT_PATTERN.split(interfaceLogRecord, 0);
                    // 避免CPU占用过高
                    if (0 == ((++i) % 100000)) {
                        Thread.sleep(80);
                        i = 0;
                    }

                    // 跳过无效记录（如果有的话）
                    if (recordParts.length < 7) {
                        continue;
                    }

                    // 只考虑表示发送请求给selfDevice所指定的系统的记录
                    if (("request".equals(recordParts[2])) &&
                            (recordParts[6].startsWith(selfDevice))) {
                        timeStamp = recordParts[0];
                        timeStamp = new String(
                                timeStamp.substring(0, 19).toCharArray());
                        String operName = recordParts[4];
                        reqCounter = repository.get(timeStamp);
                        if (null == reqCounter) {
                            reqCounter = new AtomicInteger(0);
                            existingReqCounter = repository
                                    .putIfAbsent(timeStamp, reqCounter);
                            if (null != existingReqCounter) {
                                reqCounter = existingReqCounter;
                            }
                        }

                        if (isSrcDeviceEligible(recordParts[5])) {
                            if (excludedOperationNames
                                    .contains(operName + ',')) {
                                continue;
                            }

                            if ("*".equals(includedOperationNames)) {
                                reqCounter.incrementAndGet();
                            } else {
                                if (includedOperationNames
                                        .contains(operName + ',')) {
                                    reqCounter.incrementAndGet();
                                }
                            }
                        }
                    }
                }

            } finally {
                terminationToken.reservations.decrementAndGet();
                logFileReader.close();
            }
        }

        // 判断目标设备名是否在待统计之列
        private boolean isSrcDeviceEligible(String sourceNE) {
            boolean result = "*".equals(destinationSysName) ? true
                    : destinationSysName.equals(sourceNE);
            return result;
        }
    }
}