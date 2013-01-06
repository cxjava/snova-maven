/**
 * 
 */
package org.snova.ipaddress;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xinchen
 *
 */
public class IpAddressServlet extends HttpServlet {
	private static String pattern;

	private static String getContent() {
		if (null == pattern) {
			InputStream is = IpAddressServlet.class.getResourceAsStream("/template/ipaddress.html.template");
			byte[] buffer = new byte[64 * 1024];
			try {
				int len = is.read(buffer);
				pattern = new String(buffer, 0, len);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pattern;
	}

	private String[] getIP(String host) {
		try {
			InetAddress[] addrs = InetAddress.getAllByName(host);
			String[] ret = new String[addrs.length];
			for (int i = 0; i < addrs.length; i++) {
				ret[i] = addrs[i].getHostAddress();
			}
			return ret;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pattern = getContent();
		String host = req.getParameter("ipAddressList");
		if (null != host && host.length() > 0) {
			String [] domains=host.split(System.getProperty("line.separator"));
			StringBuilder buffer = new StringBuilder();
			for(String domain:domains){
				domain=domain.trim();
				if(!"".equals(domain)){
					String[] ips = getIP(domain);
					if(ips!=null){
						for (int i = 0; i < ips.length; i++) {
							buffer.append(ips[i]).append(" ").append(domain).append(System.getProperty("line.separator"));
						}
					}
				}
			}
			String result = pattern;
			result = result.replace("${CONTENT}", buffer.toString());
			resp.setContentLength(result.length());
			resp.getOutputStream().print(result);
			return;
		}
		resp.setStatus(400);
		resp.getOutputStream().print("No Host para.");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String host = req.getParameter("Domain");
		if (null != host && host.length() > 0) {
			String[] ips = getIP(host);
			StringBuilder buffer = new StringBuilder();
			buffer.append("[");
			for (int i = 0; i < ips.length; i++) {
				buffer.append("\"");
				buffer.append(ips[i]);
				buffer.append("\"");
				if (i != ips.length - 1) {
					buffer.append(",");
				}
			}
			buffer.append("]");
			resp.setContentLength(buffer.length());
			resp.getOutputStream().print(buffer.toString());
			return;
		}
		resp.setStatus(400);
		resp.getOutputStream().print("No Host para.");
	}
}
