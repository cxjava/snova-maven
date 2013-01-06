package com.cx;

import java.net.*;

public class TestNet {
	InetAddress myIPaddress = null;
	InetAddress myServer = null;

	public static void main(String[] args) {
		TestNet mytool;
		mytool = new TestNet();
		String url;
		if (args.length > 0) {
			url = args[0];
		} else {
			url = "www.baidu.com";
		}
		System.out.println("Your host IP is: " + mytool.getMyIP());
		System.out.println("The Server IP is :" + mytool.getServerIP(url));
	}

	// 取得LOCALHOST的IP地址
	public InetAddress getMyIP() {
		try {
			myIPaddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
		}
		return (myIPaddress);
	}

	// 取得 www.baidu.com 的IP地址
	public InetAddress getServerIP(String url) {
		try {
			myServer = InetAddress.getByName(url);
			InetAddress[] addrs = InetAddress.getAllByName(url);
			String[] ret = new String[addrs.length];
			for (int i = 0; i < addrs.length; i++) {
				ret[i] = addrs[i].getHostAddress();
				System.out.println(ret[i]);
			}
		} catch (UnknownHostException e) {
		}
		return (myServer);
	}
}