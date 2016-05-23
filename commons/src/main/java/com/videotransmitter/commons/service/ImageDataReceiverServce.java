package com.videotransmitter.commons.service;

import com.videotransmitter.commons.dts.ImageDataDts;
import com.videotransmitter.commons.dts.ImageDs;
import com.videotransmitter.commons.receiver.Receiver;
import com.videotransmitter.commons.receiver.listener.ReceiverListener;

public interface ImageDataReceiverServce {
	Receiver createReceiver(int port, ReceiverListener listener);

	ImageDs convertImageDataDtsToImageDs(ImageDataDts dts);
}
