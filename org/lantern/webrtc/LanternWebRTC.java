package org.lantern.webrtc;

import org.webrtc.*;

import java.io.File;
import java.util.Map;
import java.util.EnumSet;
import java.util.LinkedList;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.lang.Thread;

public class LanternWebRTC {
    private static final String STUN_SERVER = "stun:stun.l.google.com:19302";
    private PeerConnectionFactory f;
    private MediaConstraints pcConstraints;
    private LinkedList<PeerConnection.IceServer> iceServers;

    public LanternWebRTC() {
        this.f = new PeerConnectionFactory();
        this.pcConstraints = new MediaConstraints();
        this.pcConstraints.optional.add(
                new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
        this.iceServers = new LinkedList<PeerConnection.IceServer>();
        iceServers.add(new PeerConnection.IceServer(STUN_SERVER));
        iceServers.add(new PeerConnection.IceServer("turn:fake.example.com", "fakeUsername", "fakePassword"));

    }

    public void createPeerConnection(String type) {
        Observer observer =
            new Observer("PCTest:" + type);

        PeerConnection pc = this.f.createPeerConnection(
            this.iceServers, this.pcConstraints, observer
        );

        SDPHandler sdp = new SDPHandler();

        if (type.equals("offerer")) {
            DataChannel offerDC = pc.createDataChannel(
                "offeringDC", new DataChannel.Init()
            );
            observer.setDataChannel(offerDC);
            pc.createOffer(sdp, new MediaConstraints());
            sdp.await();
            SessionDescription offerSdp = sdp.getSdp();
            System.out.println(offerSdp);
            //this.sendMessage(offerDC, "hello!");
        }
        else {
            /* answer */
            sdp.await();
            pc.createAnswer(sdp, new MediaConstraints());
            //sdp.await();

        }
    }

    public void shutDown() {
        this.f.dispose();
        System.gc();
    }

    public void sendMessage(DataChannel dc, String text) {
        ByteBuffer data = ByteBuffer.wrap(text.getBytes(Charset.forName("UTF-8")));
        DataChannel.Buffer buffer = new DataChannel.Buffer(
                data, false
        );
        dc.send(buffer);
    }

    public static void main(String[] args) {
        String type = args[0];
        LanternWebRTC rtc = new LanternWebRTC();
        rtc.createPeerConnection(type);
        //rtc.createPeerConnection();
    }

}
