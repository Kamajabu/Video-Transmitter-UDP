package com.videotransmitter.commons.dts;

public class NetAdresDs {
	private int port;
	private String ip;

	public NetAdresDs() {
	}

	public NetAdresDs(String ip, int port) {
		this.port = port;
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
