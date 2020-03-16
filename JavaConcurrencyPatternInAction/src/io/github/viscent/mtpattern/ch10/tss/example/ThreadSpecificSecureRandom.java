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

package io.github.viscent.mtpattern.ch10.tss.example;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ThreadSpecificSecureRandom {
    // 该类的唯一实例
    public static final ThreadSpecificSecureRandom INSTANCE =
            new ThreadSpecificSecureRandom();

    /*
     * SECURE_RANDOM相当于模式角色：ThreadSpecificStorage.TSObjectProxy。
     * SecureRandom相当于模式角色：ThreadSpecificStorage.TSObject。
     */
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM =
            new ThreadLocal<SecureRandom>() {

                @Override
                protected SecureRandom initialValue() {
                    SecureRandom srnd;
                    try {
                        srnd = SecureRandom.getInstance("SHA1PRNG");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        srnd = new SecureRandom();
                    }
                    return srnd;
                }

            };

    // 私有构造器
    private ThreadSpecificSecureRandom() {

    }

    public int nextInt(int upperBound) {
        SecureRandom secureRnd = SECURE_RANDOM.get();
        return secureRnd.nextInt(upperBound);
    }

    public void setSeed(long seed) {
        SecureRandom secureRnd = SECURE_RANDOM.get();
        secureRnd.setSeed(seed);
    }

}