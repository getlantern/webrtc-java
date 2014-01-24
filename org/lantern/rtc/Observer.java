package org.lantern.rtc;

import org.webrtc.PeerConnection;
import org.webrtc.DataChannel;
import org.webrtc.MediaStream;
import org.webrtc.IceCandidate;
import java.util.LinkedList;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.SignalingState;
import java.nio.ByteBuffer;
 
 
public class Observer implements PeerConnection.Observer,DataChannel.Observer {
    private String name;
    public DataChannel dataChannel;
    public LinkedList<IceCandidate> candidates = new LinkedList<IceCandidate>();
    private PeerConnection pc;

    public Observer(String name) {
        this.name = name;           
    }

    public void setPeerConnection(PeerConnection pc) {
        this.pc = pc;
    }

    @Override
    public synchronized void onRenegotiationNeeded() {
        //System.out.println("DEBUG: Renegotiation Needed");

    }

    @Override
    public synchronized void onDataChannel(DataChannel remoteChannel) {
        //System.out.println("DATA channel!!!");
        this.setDataChannel(remoteChannel);

    }

    public synchronized void setDataChannel(DataChannel dataChannel) {
        this.dataChannel = dataChannel;
        this.dataChannel.registerObserver(this);
        //System.out.println("Setting data channel: " + dataChannel.label());
    }                         

    @Override
    public synchronized void onIceGatheringChange(IceGatheringState newState) {
        //System.out.println("DEBUG: ICE Gathering state " + newState);
        if (newState == IceGatheringState.GATHERING) {
            return;
        }

    }

    @Override
    public synchronized void onIceConnectionChange(IceConnectionState newState) {

    }

    @Override
    public synchronized void onSignalingChange(SignalingState newState) {
        System.out.println("DEBUG:  SIGNAL CHANGE"); 

    }

    @Override
    public synchronized void onAddStream(MediaStream stream) {
        //System.out.println("DEBUG:  ADD STREAM");


    }

    @Override
    public synchronized void onError() {

        System.out.println("DEBUG: Error");
    }

    @Override
    public synchronized void onIceCandidate(IceCandidate candidate) {
        /*System.out.println("NEW ICE Candidate");
        System.out.println(candidate.sdpMLineIndex);
        System.out.println(candidate.sdpMid);
        System.out.println(candidate.sdp);
        System.out.println("END NEW ICE Candidate");*/
        this.candidates.add(candidate);
    }

    @Override
    public synchronized void onRemoveStream(MediaStream stream) {


    }


    @Override
    public void onStateChange() {
        //System.out.println("DEBUG: state change " + dataChannel.state());

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        System.out.println("DEBUG: message received " + buffer.data);

    }

}

