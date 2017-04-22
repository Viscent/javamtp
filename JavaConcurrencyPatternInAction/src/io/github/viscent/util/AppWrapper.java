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

package io.github.viscent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class AppWrapper {

	public static void main(String[] args) throws FileNotFoundException {
		String[] passArgs = new String[args.length - 2];
		passArgs = Arrays.copyOfRange(args, 3, args.length);

		int preSleep = Integer.valueOf(args[0]);
		int postSleep = Integer.valueOf(args[1]);
		String destClass = args[2];
		
		String stdInFile=System.getProperty("std.in");
		if(null!=stdInFile){
			System.setIn(new FileInputStream(new File(stdInFile)));
		}

		try {
			Thread.sleep(preSleep);
		} catch (InterruptedException e) {
		}
		
	
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			Method method = Class.forName(destClass).getMethod("main",
			    String[].class );

			System.out.println("Started at:" + sdf.format(new Date()));
			method.invoke(null, new Object[]{passArgs});
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		System.out.println("Finished at:" + sdf.format(new Date()));

		try {
			Thread.sleep(postSleep);
		} catch (InterruptedException e) {
		}

	}

}
