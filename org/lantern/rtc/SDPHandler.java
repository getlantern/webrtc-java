package org.lantern.webrtc;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
 

public class SDPHandler implements SdpObserver {
    private boolean success = false;
    private SessionDescription sdp = null;
    private String error = null;
    private CountDownLatch latch = new CountDownLatch(1);

    public SDPHandler() {}

    @Override
    public void onCreateSuccess(SessionDescription sdp) {
        this.sdp = sdp;
        onSetSuccess();
    }

    @Override
    public void onSetSuccess() {
        success = true;
        latch.countDown();
    }

    @Override
    public void onCreateFailure(String error) {
        onSetFailure(error);
    }

    @Override
    public void onSetFailure(String error) {
        this.error = error;
        latch.countDown();
    }

    public boolean await() {
        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
            return getSuccess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getSuccess() {
        return success;
    }

    public SessionDescription getSdp() {
        return sdp;
    }

    public String getError() {
        return error;
    }
}

