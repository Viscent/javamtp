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

package io.github.viscent.mtpattern.ch14.hsha.example;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import io.github.viscent.mtpattern.ch14.hsha.AsyncTask;
import io.github.viscent.util.Debug;

public class SampleAsyncTask {

    public static void main(String[] args) {
        XAsyncTask task = new XAsyncTask();
        List<Future<String>> results = new LinkedList<Future<String>>();

        try {
            results.add(task.doSomething("Half-sync/Half-async", 1));

            results.add(task.doSomething("Pattern", 2));

            for (Future<String> result : results) {
                Debug.info(result.get());
            }

            Thread.sleep(200);
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private static class XAsyncTask extends AsyncTask<String> {

        @Override
        protected String doInBackground(Object... params) {
            String message = (String) params[0];
            int sequence = (Integer) params[1];
            Debug.info("doInBackground:" + message);
            return "message " + sequence + ":" + message;
        }

        @Override
        protected void onPreExecute(Object... params) {
            String message = (String) params[0];
            int sequence = (Integer) params[1];
            Debug.info("onPreExecute:[" + sequence + "]" + message);
        }

        public Future<String> doSomething(String message, int sequence) {
            if (sequence < 0) {
                throw new IllegalArgumentException(
                        "Invalid sequence:" + sequence);
            }
            return this.dispatch(message, sequence);
        }
    }
}