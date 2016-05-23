package com.videotransmitter.commons.sender.impl;

import com.videotransmitter.commons.dts.ImageDataDts;
import com.videotransmitter.commons.sender.Sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UDPSender implements Sender, Runnable {
	private DatagramSocket socket;
	private int port;
	private InetAddress inetAddress;
	private List<DatagramPacket> datagramBuffer = new ArrayList<>();
	private boolean isStop;
	private static UDPSender self;
	private static final Logger LOGGER = Logger.getLogger(UDPSender.class.getName());

	private UDPSender() {
		new Thread(this).start();
	}

	public static UDPSender newInstance() {
		if (self == null) {
			self = new UDPSender();
		}
		return self;
	}

	@Override
	public void send(ImageDataDts imageDataDts) {
		copyData(imageDataDts);
		synchronized (this) {
			this.notify();
		}
	}

	private void copyData(ImageDataDts imageDataDts) {
		LOGGER.info("Wysylanie ramki skladajacej sie z " + imageDataDts.getByteBufferList().size() + " pakietow.");
		for (ByteBuffer elem : imageDataDts.getByteBufferList()) {
			byte[] byteArray = elem.array();
			datagramBuffer.add(new DatagramPacket(byteArray, byteArray.length, inetAddress, port));
		}
	}

	DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	private void sendData() {
		try {
			socket.send(datagramBuffer.get(0));
			datagramBuffer.remove(0);
		} catch (IOException e) {
			System.err.println("BĹ‚Ä…d podczas wysyĹ‚ania pakietu!");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (!isStop) {
			if (datagramBuffer.size() == 0) {
				waitForNewFrame();
			}
			sendData();
		}
		socket.close();
	}

	private void waitForNewFrame() {
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				System.err.println("BĹ‚Ä…d podczas oczekiwania na nowÄ… ramkÄ™!");
				e.printStackTrace();
			}
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	@Override
	public void stop() {
		this.isStop = true;
	}
}
