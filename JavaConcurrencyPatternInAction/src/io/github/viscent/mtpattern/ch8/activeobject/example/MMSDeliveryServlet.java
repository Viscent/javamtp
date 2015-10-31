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

package io.github.viscent.mtpattern.ch8.activeobject.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MMSDeliveryServlet extends HttpServlet {

	private static final long serialVersionUID = 5886933373599895099L;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		//将请求中的数据解析为内部对象
		MMSDeliverRequest mmsDeliverReq = this.parseRequest(req.getInputStream());
		Recipient shortNumberRecipient = mmsDeliverReq.getRecipient();
		Recipient originalNumberRecipient = null;

		try {
			// 将接收方短号转换为长号
			originalNumberRecipient = convertShortNumber(shortNumberRecipient);
		} catch (SQLException e) {

			// 接收方短号转换为长号时发生数据库异常，触发请求消息的缓存
			AsyncRequestPersistence.getInstance().store(mmsDeliverReq);

			//省略其它代码
			
			resp.setStatus(202);
		}

		System.out.println(originalNumberRecipient);
	}

	private MMSDeliverRequest parseRequest(InputStream reqInputStream) {
		MMSDeliverRequest mmsDeliverReq = new MMSDeliverRequest();
		//省略其它代码
		return mmsDeliverReq;
	}

	private Recipient convertShortNumber(Recipient shortNumberRecipient)
			throws SQLException {
		Recipient recipent = null;
		//省略其它代码
		return recipent;
	}

}
