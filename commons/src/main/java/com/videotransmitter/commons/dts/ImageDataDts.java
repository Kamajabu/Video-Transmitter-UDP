package com.videotransmitter.commons.dts;

import java.nio.ByteBuffer;
import java.util.List;

import com.videotransmitter.commons.constans.StandardCnstans;

/* id(short)|height(short)|width(short)|layers(short)|nr_px_start(int)|data(byte...) */
public class ImageDataDts implements Cloneable {
	private static int preByteSize = StandardCnstans.STANDARD_PRE_BYTE_SIZE;
	private int width;
	private int height;
	private int layers;
	private List<ByteBuffer> byteBufferList;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getLayers() {
		return layers;
	}

	public void setLayers(int layers) {
		this.layers = layers;
	}

	public List<ByteBuffer> getByteBufferList() {
		return byteBufferList;
	}

	public void setByteBufferList(List<ByteBuffer> byteBufferList) {
		this.byteBufferList = byteBufferList;
	}

	public static int getPreByteSize() {
		return preByteSize;
	}

	public static void setPreByteSize(int preByteSize) {
		ImageDataDts.preByteSize = preByteSize;
	}

}
