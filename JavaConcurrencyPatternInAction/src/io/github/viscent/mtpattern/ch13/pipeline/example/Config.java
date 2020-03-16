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

public class Config {

    public static int MAX_RECORDS_PER_FILE = 5000;

    /**
     * RECORD_SAVE_CHUNK_SIZE should be less than RECORD_JOIN_SIZE
     */
    public static int RECORD_SAVE_CHUNK_SIZE = 350;

    public static int WRITER_BUFFER_SIZE = 8192 * 10;

    /**
     * RECORD_JOIN_SIZE should be about WRITER_BUFFER_SIZE/103
     */
    public static int RECORD_JOIN_SIZE = 700;// 此值不能过小，过小会导致内存消耗过大 =8196/101

    /*
     * CPU<40%
     * 
     * public static int RECORD_SAVE_CHUNK_SIZE=100;
     * 
     * public static int RECORD_JOIN_SIZE=300;
     */

}
