package com.videotransmitter.commons.service.impl;

import com.videotransmitter.commons.constans.StandardCnstans;
import com.videotransmitter.commons.dts.ImageDataDts;
import com.videotransmitter.commons.dts.ImageDs;
import com.videotransmitter.commons.receiver.Receiver;
import com.videotransmitter.commons.receiver.impl.UDPReceiver;
import com.videotransmitter.commons.receiver.listener.ReceiverListener;
import com.videotransmitter.commons.service.ImageDataReceiverServce;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ImageDataReceiverServceImpl implements ImageDataReceiverServce {

	@Override
	public Receiver createReceiver(int port, ReceiverListener listener) {
		UDPReceiver uDPReceiver = UDPReceiver.newInstance();
		try {
			DatagramSocket datagramSocket = new DatagramSocket(port);
			uDPReceiver.setSocket(datagramSocket);
			uDPReceiver.setListener(listener);
		} catch (SocketException e) {
			System.err.println("Błąd podczas inicjalizacji DatagramSocket");
			e.printStackTrace();
		}
		return uDPReceiver;
	}

	@Override
	public ImageDs convertImageDataDtsToImageDs(ImageDataDts dts) {
		int height = dts.getHeight();
		int width = dts.getWidth();
		int layers = dts.getLayers();
		int limit;
		ByteBuffer imageData = ByteBuffer.allocate(height * width * layers);
		// sortData(dts.getByteBufferList());
		for (ByteBuffer buffer : dts.getByteBufferList()) {
			if (height * width * layers - imageData.position() >= StandardCnstans.UDP_CUSTOM_SIZE) {
				limit = StandardCnstans.UDP_CUSTOM_SIZE;
			} else {
				limit = height * width * layers - imageData.position() + ImageDataDts.getPreByteSize();
			}
			byte[] copyOfRange = Arrays.copyOfRange(buffer.array(), ImageDataDts.getPreByteSize(), limit);
			imageData.put(copyOfRange);
		}

		ImageDs imageDs = new ImageDs();
		imageDs.setHeight(height);
		imageDs.setWidth(width);
		imageDs.setLayers(layers);
		imageDs.setData(imageData.array());

		return imageDs;
	}

//	private void sortData(List<ByteBuffer> byteBufferList) {
//		byteBufferList.sort(new Comparator<ByteBuffer>() {
//
//			@Override
//			public int compare(ByteBuffer o1, ByteBuffer o2) {
//				return getNrPxStart(o1) - getNrPxStart(o2);
//			}
//		});
//	}

	private int getNrPxStart(ByteBuffer buffer) {
		byte[] b = new byte[4];
		return buffer.get(b, 8, 12).getInt(); // nrPxStart
	}
}
