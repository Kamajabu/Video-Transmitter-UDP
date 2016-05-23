package com.videotransmitter.commons.receiver.listener;

import com.videotransmitter.commons.dts.ImageDataDts;

public interface ReceiverListener {
	void onFrameReady(ImageDataDts receivedImageDataDts);
}
