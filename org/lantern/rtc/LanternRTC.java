package org.lantern.rtc;

import org.webrtc.*;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

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
        public Observer obs;

        /*  No need to pass DTLS constraint as it is on by default in
         *  Chrome M31.
         *  For SCTP, reliable and ordered is true by default. */  
        public void createDataChannel(String label) {
            this.dc = this.pc.createDataChannel(label, new DataChannel.Init());
            this.obs.setDataChannel(this.dc);
        }

        public void registerDataChannel(DataChannel dc) {
            this.dc = dc;
            this.dc.registerObserver(this.obs);
        }

        public void createOffer() {
            this.pc.createOffer(this.sdp, new MediaConstraints());
        }

        public void createAnswer() {
            this.sdp = new SDPHandler();
            this.pc.createAnswer(this.sdp, new MediaConstraints());
            this.sdpWait();
        }

        public void sdpWait() {
            this.sdp.await();
        }

        public void waitDelaysFinish() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void onIceCandidate(IceCandidate c) {
            System.out.println("ICe cand");

        }

        public void setRemoteDescription(SessionDescription desc) {
            this.pc.setRemoteDescription(this.sdp, desc);
            this.sdpWait();
        }

        public void setLocalDescription(SessionDescription desc) {

            this.sdp = new SDPHandler();
            this.pc.setLocalDescription(this.sdp, desc);
            this.sdpWait();
        }

        public LanternRTCPeer(PeerConnectionFactory f, String obsName, LinkedList<PeerConnection.IceServer> iceServers) {
            this.sdp = new SDPHandler();
            this.mc = new MediaConstraints();
            this.mc.mandatory.add(
                new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true")
            );

            /* this needs replaced with
             * this.mc.optional.add(
             *   new MediaConstraints.KeyValuePair(
             *   "internalSctpDataChannels", "true"
             *   )
             * )
             * when supported
             *
             */
            this.mc.optional.add(
                new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
            this.obs = new Observer(obsName);
            this.pc = f.createPeerConnection(
                iceServers, this.mc, this.obs
            );
        }

        public boolean sendMessage(String message) {
            DataChannel.Buffer buffer = new DataChannel.Buffer(
                    ByteBuffer.wrap(message.getBytes(Charset.forName("UTF-8"))), false);
            return this.dc.send(buffer);
        }


        public void sendFile(File f) {

        }

        public void sdpClear() {
            this.sdp = new SDPHandler();
        }

        public void addCandidates(LanternRTCPeer peer) {
            for (IceCandidate candidate : this.obs.candidates) {
                peer.pc.addIceCandidate(candidate);
            }
            this.obs.candidates.clear();
        }


        public void close() {
            if (this.dc != null) {
                System.out.println("DEBUG: Closing data channel");
                this.dc.close();
            }
            this.pc.dispose();
        }

    }

    @Test
    public void createSession() {


        /* create ICE servers
         * using Google's public STUN server 
         */
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
        iceServers.add(new PeerConnection.IceServer(
                    "stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer(
                    "turn:fake.example.com", "fakeUsername", "fakePassword"));


        LanternRTCPeer p1 = new LanternRTCPeer(f, "PCTest:offerer", iceServers);
        p1.createDataChannel("testing");
        p1.createOffer();
        p1.sdpWait();
        SessionDescription offerSdp = p1.sdp.getSdp();
        /* get session description */

        LanternRTCPeer p2 = new LanternRTCPeer(f, "PCTest:answerer", iceServers);
        p2.setRemoteDescription(offerSdp);
        p2.createAnswer();
        p2.sdpWait();
        SessionDescription answerSdp = p2.sdp.getSdp();
        p2.setLocalDescription(answerSdp);

        p1.setLocalDescription(offerSdp);
        p1.setRemoteDescription(answerSdp);

        /* make sure candidates are added to each other */
        p1.addCandidates(p2);
        p2.addCandidates(p1);

        /* small delay for connection setup before 
         * exchanging a message */
        p1.waitDelaysFinish();

        /* message successfully sent over data channel */
        assertTrue(p1.sendMessage("hey"));

        /* close data channels 
         * and shut down peer connection
         * */
        p1.close();
        p2.close();

    }

    public static void main(String[] args) {
        LanternRTC rtc = new LanternRTC();
        rtc.createSession();
        
        /* clean up */
        rtc.f.dispose();
        System.gc();

    }

}
