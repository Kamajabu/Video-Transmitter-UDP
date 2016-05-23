package com.videotransmitter.commons.sender;

import java.net.InetAddress;

import com.videotransmitter.commons.dts.ImageDataDts;

public interface Sender {
	void send(ImageDataDts imageDataDts);

	void stop();

	InetAddress getInetAddress();

	int getPort();

}
