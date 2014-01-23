package org.lantern.rtc;

import org.webrtc.*;

import java.io.File;
import java.util.Map;
import java.util.EnumSet;
import java.util.List;
import java.util.LinkedList;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.lang.Thread;

public class LanternRTC {
    private static final String STUN_SERVER = "stun:stun.l.google.com:19302";
    public PeerConnectionFactory f = new PeerConnectionFactory();


    private class LanternRTCPeer {
        private String description;
        public SDPHandler sdp;
        private MediaConstraints mc;
        private PeerConnection pc;
        private DataChannel dc;
        private LinkedList<PeerConnection.IceServer> iceServers;
        private Observer obs;


        /*  No need to pass DTLS constraint as it is on by default in
         *  Chrome M31.
         *  For SCTP, reliable and ordered is true by default. */  
        public void createDataChannel(String label) {
            this.dc = this.pc.createDataChannel(label, new DataChannel.Init());
        }

        public void registerDataChannel(DataChannel dc) {
            this.dc = dc;
            this.dc.registerObserver(this.obs);
        }

        public void createOffer() {
            this.pc.createOffer(this.sdp, new MediaConstraints());
        }

        public void createAnswer() {
            this.pc.createAnswer(this.sdp, new MediaConstraints());
        }

        public void addFakeIceServers() {
            iceServers.add(new PeerConnection.IceServer(STUN_SERVER));
            iceServers.add(new PeerConnection.IceServer("turn:fake.example.com", "fakeUsername", "fakePassword"));
        }       

        public void sdpWait() {
            this.sdp.await();
        }

        public void onIceCandidate(IceCandidate c) {

        }

        public void setRemoteDescription(SessionDescription desc) {
            this.pc.setRemoteDescription(this.sdp, desc);
        }

        public void setLocalDescription() {

            this.pc.setLocalDescription(this.sdp, this.sdp.getSdp());
        }

        public LanternRTCPeer(PeerConnectionFactory f) {
            this.sdp = new SDPHandler();
            this.mc = new MediaConstraints();
            this.mc.optional.add(
                new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
            this.iceServers = new LinkedList<PeerConnection.IceServer>();

            this.addFakeIceServers();
            this.obs = new Observer("blah");
            this.pc = f.createPeerConnection(
                this.iceServers, this.mc, this.obs
            );
        }

        public void sendMessage(String message) {
            DataChannel.Buffer buffer = new DataChannel.Buffer(
                    ByteBuffer.wrap(message.getBytes(Charset.forName("UTF-8"))), false);
            this.dc.send(buffer);
        }


        public void sendFile(File f) {

        }

        public void clearSDP() {
            this.sdp = new SDPHandler();
        }


        public void closeChannel() {
            if (this.dc != null) {
                System.out.println("DEBUG: Closing data channel");
                this.dc.close();
            }
        }

    }

    public void createSession() {
        LanternRTCPeer p1 = new LanternRTCPeer(f);
        p1.createDataChannel("testing");
        p1.createOffer();
        p1.sdpWait();
        /* get session description */
        String desc = p1.sdp.getSdp().description;
        System.out.println(desc);

        LanternRTCPeer p2 = new LanternRTCPeer(f);
        p2.setRemoteDescription(p1.sdp.getSdp());
        p2.sdpWait();
        p2.clearSDP();
        p2.sdpWait();
        p2.createAnswer();
        p2.sdpWait();
        p2.setLocalDescription();
        p2.sdpWait();


        p1.sendMessage("hey");
        p2.sdpWait();
        p1.sdpWait();
        p1.closeChannel();
        p2.closeChannel();

    }

    public static void main(String[] args) {
        LanternRTC rtc = new LanternRTC();
        rtc.createSession();

        //rtc.f.dispose();
        System.gc();

    }

}
