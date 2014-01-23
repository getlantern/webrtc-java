package org.lantern.webrtc;

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
    private DataChannel dataChannel;
    public LinkedList<IceCandidate> candidates;
    private PeerConnection pc;

    public Observer(String name) {
        this.name = name;           
        this.candidates = new LinkedList<IceCandidate>();
        this.pc = null;
    }

    public void setPeerConnection(PeerConnection pc) {
        this.pc = pc;
    }

    @Override
    public synchronized void onRenegotiationNeeded() {
        System.out.println("DEBUG: Renegotiation Needed");

    }

    @Override
    public synchronized void onDataChannel(DataChannel remoteChannel) {
        System.out.println("DATA channel!!!");

    }

    public synchronized void setDataChannel(DataChannel dataChannel) {
        this.dataChannel = dataChannel;
        this.dataChannel.registerObserver(this);
        System.out.println("DEBUG: " + dataChannel.label());
    }                         

    public synchronized DataChannel getDataChannel() {
        return this.dataChannel;
    }

    @Override
    public synchronized void onIceGatheringChange(IceGatheringState newState) {
        System.out.println("DEBUG: ICE Gathering state " + newState);
        if (newState == IceGatheringState.GATHERING) {
            return;
        }

    }

    @Override
    public synchronized void onIceConnectionChange(IceConnectionState newState) {
        System.out.println("DEBUG: Ice connection change");

    }

    @Override
    public synchronized void onSignalingChange(SignalingState newState) {
        System.out.println("DEBUG:  SIGNAL CHANGE"); 

    }

    @Override
    public synchronized void onAddStream(MediaStream stream) {
        System.out.println("DEBUG:  ADD STREAM");


    }

    @Override
    public synchronized void onError() {

        System.out.println("DEBUG: Error");
    }

    @Override
    public synchronized void onIceCandidate(IceCandidate candidate) {
        System.out.println("NEW ICE Candidate");
        System.out.println(candidate);
        System.out.println("END NEW ICE Candidate");
        //SDPHandler sdp = new SDPHandler();
        //this.pc.createOffer(sdp, new MediaConstraints());
        this.candidates.add(candidate);
    }

    @Override
    public synchronized void onRemoveStream(MediaStream stream) {


    }


    @Override
    public void onStateChange() {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        System.out.println("MSG: " + buffer.data);

    }

}

