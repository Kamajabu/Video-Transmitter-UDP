package com.videotransmitter.commons.receiver;

public interface ReceiverListener {
	public void onFrameReady(byte[] received);
}
