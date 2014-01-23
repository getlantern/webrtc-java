package org.lantern.rtc;

import org.webrtc.*;

import java.io.File;
import java.util.Map;
import java.util.EnumSet;
import java.util.LinkedList;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.lang.Thread;

public class LanternRTC {
    private static final String STUN_SERVER = "stun:stun.l.google.com:19302";
    private PeerConnectionFactory f;
    private PeerConnection pc;
    private Observer obsoff, obsans;
    private MediaConstraints pcConstraints;
    private LinkedList<PeerConnection.IceServer> iceServers;
    private final IceObserver iceObserver;

    public LanternRTC() {
        this.f = new PeerConnectionFactory();
        this.pcConstraints = new MediaConstraints();
        this.pcConstraints.optional.add(
                new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
        this.iceServers = new LinkedList<PeerConnection.IceServer>();
        iceServers.add(new PeerConnection.IceServer(STUN_SERVER));
        iceServers.add(new PeerConnection.IceServer("turn:fake.example.com", "fakeUsername", "fakePassword"));

    }

    public static interface IceObserver {
        public void onIceServers(List<PeerConnection.IceServer> iceServers);
    }

    public SessionDescription createOffer() throws InterruptedException {
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
        offerPC.setLocalDescription(sdp, sdp.getSdp());
        return sdp.getSdp();
    }

    private MediaConstraints createConstraints() {
        MediaConstraints pcConstraints = new MediaConstraints();
        pcConstraints.optional.add(
                new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
        return pcConstraints;
    }

    public void createAnswer(SessionDescription offerSdp) throws InterruptedException {
        this.obsans = 
            new Observer("PCTest: answerer");
        this.answerPC = this.f.createPeerConnection(
                this.iceServers, this.pcConstraints, obsans
        );
        this.obsans.setPeerConnection(this.answerPC);
        SDPHandler sdp = new SDPHandler();
        this.answerPC.createOffer(sdp, this.createConstraints());
        sdp.await();
        this.answerPC.setLocalDescription(sdp, sdp.getSdp());
        sdp.await();
        System.out.println("**** PRINTING ICE CANDS *****");
        for (IceCandidate candidate : this.obsans.candidates) {
            System.out.println(candidate);
        }
        System.out.println("**** DONE PRINTING ****");

        /* process offer sdp 
        sdp = new SDPHandler();
        answerPC.setRemoteDescription(sdp, offerSdp);
        sdp.await();         */

        /* send answer SDP back to offerer 
        sdp = new SDPHandler();
        answerPC.createAnswer(sdp, new MediaConstraints());
        sdp.await();
        SessionDescription answerSdp = sdp.getSdp();
        System.out.println(answerSdp);
        answerPC.setLocalDescription(sdp, answerSdp);
        sdp.await();                       */
    }

    public void createPeerSession(String type) throws InterruptedException {
        if (type.equals("offer")) {
            SessionDescription offerSdp = this.createOffer();
        } else {
            this.createAnswer(null);
        }
        //this.createAnswer(offerSdp);
        //this.obsoff.getDataChannel().close();
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

}
