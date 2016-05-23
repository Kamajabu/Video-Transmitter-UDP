package com.videotransmitter.commons.receiver.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.videotransmitter.commons.constans.StandardCnstans;
import com.videotransmitter.commons.dts.ImageDataDts;
import com.videotransmitter.commons.receiver.Receiver;
import com.videotransmitter.commons.receiver.listener.ReceiverListener;

public class UDPReceiver implements Runnable, Receiver {
	private DatagramSocket socket;
	ByteBuffer receivedByteBuffer = ByteBuffer.allocate(StandardCnstans.UDP_CUSTOM_SIZE);
	private byte[] received = new byte[StandardCnstans.UDP_CUSTOM_SIZE];
	private boolean isStop;
	private static UDPReceiver self;
	ImageDataDts receivedImageDataDts = new ImageDataDts();
	Map<Short, ImageDataDts> receivedImageDataMap = new HashMap<>();
	int spaceForOryginalDataInOneFrame;
	int numberOfListElement;
	ReceiverListener listener;

	private static final Logger LOGGER = Logger.getLogger(UDPReceiver.class.getName());

	private UDPReceiver() {
	}

	public static UDPReceiver newInstance() {
		if (self == null) {
			self = new UDPReceiver();
		}
		LOGGER.info("Utworzono Receiver");
		return self;
	}

	@Override
	public void start() {
		if (socket == null) {
			throw new InternalError("Socket can`t be null!");
		}
		new Thread(this).start();
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		LOGGER.info("Watek receiver`a: start");
		while (!isStop) {
			byte[] receiveData = receiveData();
			processReceivedData(receiveData);

			notifyAllThreads();
		}
		LOGGER.info("Watek receiver`a: stop");
		socket.close();
	}

	private void processReceivedData(byte[] data) {
		/* read id */
		receivedByteBuffer = ByteBuffer.wrap(data);
		short id = receivedByteBuffer.getShort();
		short height = receivedByteBuffer.getShort();
		short width = receivedByteBuffer.getShort();
		short layers = receivedByteBuffer.getShort();

		receivedImageDataDts.setHeight(height);
		receivedImageDataDts.setWidth(width);
		receivedImageDataDts.setLayers(layers);

		spaceForOryginalDataInOneFrame = StandardCnstans.UDP_CUSTOM_SIZE - ImageDataDts.getPreByteSize();
		numberOfListElement = (width * height * layers) / spaceForOryginalDataInOneFrame;
		if ((width * height * layers) % spaceForOryginalDataInOneFrame != 0) {
			numberOfListElement++;
		}

		/* add received part of frame into map */
		ImageDataDts imageDataDtsFromMap = receivedImageDataMap.get(id);
		if (imageDataDtsFromMap == null) {
			receivedImageDataDts.setByteBufferList(new ArrayList<ByteBuffer>(numberOfListElement));
			receivedImageDataDts.getByteBufferList().add(ByteBuffer.wrap(Arrays.copyOfRange(data, 0, data.length)));
			receivedImageDataMap.put(id, receivedImageDataDts);
		} else {
			receivedImageDataDts.getByteBufferList().add(ByteBuffer.wrap(Arrays.copyOfRange(data, 0, data.length)));
		}

		/* pefrorm listener if all data are received */
		if (receivedImageDataDts.getByteBufferList().size() == numberOfListElement && listener != null) {
			listener.onFrameReady(receivedImageDataDts);
			/* delete complete object from map */
			receivedImageDataMap.remove(id);
		}
	}

	private byte[] receiveData() {
		DatagramPacket datagramPacket = new DatagramPacket(received, received.length);
		try {
			socket.receive(datagramPacket);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Błąd podczas odbierania pakietu.");
			e.printStackTrace();
		}
		LOGGER.info("Odebrano ramke danych z adresu: " + datagramPacket.getAddress().getHostName() + " port: "
				+ datagramPacket.getPort());
		return datagramPacket.getData();
	}

	private void notifyAllThreads() {
		synchronized (this) {
			this.notifyAll();
		}
	}

	public void stop() {
		this.isStop = true;
	}

	public ReceiverListener getListener() {
		return listener;
	}

	public void setListener(ReceiverListener listener) {
		this.listener = listener;
	}
}
