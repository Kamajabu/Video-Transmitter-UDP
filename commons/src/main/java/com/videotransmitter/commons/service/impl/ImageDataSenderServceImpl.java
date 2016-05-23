package com.videotransmitter.commons.service.impl;

import com.videotransmitter.commons.constans.StandardCnstans;
import com.videotransmitter.commons.dts.ImageDataDts;
import com.videotransmitter.commons.dts.NetAdresDs;
import com.videotransmitter.commons.sender.Sender;
import com.videotransmitter.commons.sender.impl.UDPSender;
import com.videotransmitter.commons.service.ImageDataSenderServce;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ImageDataSenderServceImpl implements ImageDataSenderServce {
	private static int imageDataDtsCounter = 0;
	Random generator = new Random();
	int lastPosition;
	int spaceForOryginalDataInOneFrame;
	int listElementNumber;

	/**
	 * Metoda tworzy i poprawnie wypełnia gotowy obiekt do transportu obrazu.
	 */
	@Override
	public ImageDataDts createImageDataDts(int width, int height, int layers) {
		//if (imageDataDtsCounter < 2) {
			//imageDataDtsCounter++;
			ImageDataDts imageDataDts = new ImageDataDts();
			imageDataDts.setHeight(height);
			imageDataDts.setWidth(width);
			imageDataDts.setLayers(layers);
			imageDataDts.setByteBufferList(buildEmptyByteBufferList(width, height, layers));
			return imageDataDts;
		//} else {
			//throw new InternalError("Nie można utworzyć w aplikacji więcej obiektów ImageDataDts niż 2!");
		//}
	}

	private List<ByteBuffer> buildEmptyByteBufferList(int width, int height, int layers) {
		ArrayList<ByteBuffer> byteBufferList = new ArrayList<>();
		spaceForOryginalDataInOneFrame = StandardCnstans.UDP_CUSTOM_SIZE - ImageDataDts.getPreByteSize();
		int numberOfListElement = (width * height * layers) / spaceForOryginalDataInOneFrame;
		if ((width * height * layers) % spaceForOryginalDataInOneFrame != 0) {
			numberOfListElement++;
		}
		for (int i = 0; i < numberOfListElement; i++) {
			byteBufferList.add(ByteBuffer.allocate(StandardCnstans.UDP_CUSTOM_SIZE));
		}
		return byteBufferList;
	}

	@Override
	public void clearBufferAndFillData(ImageDataDts dts, byte[] data) {
		clearByteBufferList(dts.getByteBufferList());
		fillStrategyData(dts);
		splitAndFillData(dts.getByteBufferList(), data);
	}

	@Override
	public void clearBufferAndFillData(ImageDataDts dts, int[] data) {
		clearByteBufferList(dts.getByteBufferList());
		fillStrategyData(dts);
		splitAndFillData(dts.getByteBufferList(), data);
	}

	@Override
	public void clearBufferAndFillData(ImageDataDts dts, int[][] data) {
		clearByteBufferList(dts.getByteBufferList());
		fillStrategyData(dts);
		for (int[] px_y : data) {
			splitAndFillData(dts.getByteBufferList(), px_y);
		}
	}

	private void rewriteTableToByteBuffer(ByteBuffer byteBuffer, int[] data) {
		for (int px : data) {
			byteBuffer.put((byte) px);
		}
	}

	private void fillStrategyData(ImageDataDts dts) {
		short randomId = (short) (generator.nextInt() % Short.MAX_VALUE);
		for (ByteBuffer elem : dts.getByteBufferList()) {
			elem.putShort(randomId);
			elem.putShort((short) dts.getHeight());
			elem.putShort((short) dts.getWidth());
			elem.putShort((short) dts.getLayers());
		}
	}

	private void clearByteBufferList(List<ByteBuffer> list) {
		for (ByteBuffer elem : list) {
			elem.clear();
		}
	}

	private void splitAndFillData(List<ByteBuffer> list, byte[] data) {
		lastPosition = 0;
		for (ByteBuffer elem : list) {
			elem.putInt(lastPosition);
			elem.put(Arrays.copyOfRange(data, lastPosition,
					lastPosition + StandardCnstans.UDP_CUSTOM_SIZE - ImageDataDts.getPreByteSize()));
			lastPosition += (StandardCnstans.UDP_CUSTOM_SIZE - ImageDataDts.getPreByteSize());
		}
	}

	private void splitAndFillData(List<ByteBuffer> list, int[] data) {
		int oldListElementNumber = -1;
		lastPosition = 0;
		spaceForOryginalDataInOneFrame = StandardCnstans.UDP_CUSTOM_SIZE - ImageDataDts.getPreByteSize();
		for (int i = 0; i < data.length; i++) {
			listElementNumber = i / spaceForOryginalDataInOneFrame;
			if (i % spaceForOryginalDataInOneFrame != 0) {
				listElementNumber++;
			}
			if (oldListElementNumber != listElementNumber) {
				list.get(listElementNumber).putInt(i + 1);
			}
			list.get(listElementNumber).put((byte) data[i]);
			oldListElementNumber = listElementNumber;
		}
	}

	@Override
	public Sender createUDPSender(NetAdresDs netAdresDs) {
		UDPSender uDPSender = UDPSender.newInstance();
		try {
			DatagramSocket datagramSocket = new DatagramSocket();
			InetAddress inetAddress = InetAddress.getByName(netAdresDs.getIp());
			uDPSender.setSocket(datagramSocket);
			uDPSender.setInetAddress(inetAddress);
			uDPSender.setPort(netAdresDs.getPort());
		} catch (SocketException e) {
			System.err.println("Błąd podczas inicjalizacji DatagramSocket");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.err.println("Nie znaleziono hosta: " + netAdresDs.getIp());
			e.printStackTrace();
		}
		return uDPSender;
	}
}
