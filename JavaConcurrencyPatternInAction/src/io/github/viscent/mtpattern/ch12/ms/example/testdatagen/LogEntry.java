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

package io.github.viscent.mtpattern.ch12.ms.example.testdatagen;

public class LogEntry {
	public long timeStamp;
	public String interfaceType;
	public String recordType;//request or response
	public String interfaceName;
	public String operationName;
	public String srcDevice="ESB";
	public String dstDevice;
	public String traceId;
	public String selfIPAddress="192.168.1.102";
	public String calling="13612345678";
	public String callee="136712345670";
	@Override
  public String toString() {
	  return "LogEntry [timeStamp=" + timeStamp + ", interfaceType="
	      + interfaceType + ", recordType=" + recordType + ", interfaceName="
	      + interfaceName + ", operationName=" + operationName + ", srcDevice="
	      + srcDevice + ", dstDevice=" + dstDevice + ", traceId=" + traceId
	      + ", calling=" + calling + ", callee=" + callee + "]";
  }
	
	
}
