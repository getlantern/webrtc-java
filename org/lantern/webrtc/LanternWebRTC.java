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
    private PeerConnection offerPC;
    private PeerConnection answerPC;
    private Observer obsoff, obsans;
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

    public SessionDescription createOffer() {
        this.obsoff =
            new Observer("PCTest: offerer");
        this.offerPC = this.f.createPeerConnection(
            this.iceServers, this.pcConstraints, obsoff
        );
        DataChannel.Init init = new DataChannel.Init();
        DataChannel offerDC = offerPC.createDataChannel(
                "offeringDC", init
                );
        obsoff.setDataChannel(offerDC);
        SDPHandler sdp = new SDPHandler();
        /* send offer SDP to other peer */
        offerPC.createOffer(sdp, new MediaConstraints());
        sdp.await();
        return sdp.getSdp();
    }

    public void createAnswer(SessionDescription offerSdp) {
        this.obsans = 
            new Observer("PCTest: answerer");
        this.answerPC = this.f.createPeerConnection(
                this.iceServers, this.pcConstraints, obsans
        );
        SDPHandler sdp;

        /* process offer sdp */
        sdp = new SDPHandler();
        answerPC.setRemoteDescription(sdp, offerSdp);
        sdp.await();

        /* send answer SDP back to offerer */
        sdp = new SDPHandler();
        answerPC.createAnswer(sdp, new MediaConstraints());
        sdp.await();
        SessionDescription answerSdp = sdp.getSdp();
        System.out.println(answerSdp);
        answerPC.setLocalDescription(sdp, answerSdp);
        sdp.await();
    }

    public void createPeerSession(String type) {
        SessionDescription offerSdp = this.createOffer();
        this.createAnswer(offerSdp);
        this.obsoff.getDataChannel().close();
        //this.obsans.getDataChannel().close();
        //this.sendMessage(offerDC, "hello!");
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
        rtc.createPeerSession(type);
        //rtc.createPeerConnection();
    }

}
