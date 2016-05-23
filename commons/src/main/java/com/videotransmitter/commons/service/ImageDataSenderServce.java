package com.videotransmitter.commons.service;

import com.videotransmitter.commons.dts.ImageDataDts;
import com.videotransmitter.commons.dts.NetAdresDs;
import com.videotransmitter.commons.sender.Sender;

public interface ImageDataSenderServce {
	public ImageDataDts createImageDataDts(int width, int height, int layers);

	@Deprecated /* use clearBufferAndFillData(ImageDataDts dta, int[][] data) */
	public void clearBufferAndFillData(ImageDataDts dta, byte[] data);

	@Deprecated /* use clearBufferAndFillData(ImageDataDts dta, int[][] data) */
	public void clearBufferAndFillData(ImageDataDts dta, int[] data);

	public void clearBufferAndFillData(ImageDataDts dta, int[][] data);

	public Sender createUDPSender(NetAdresDs netAdresDs);
}
