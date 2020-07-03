package base.chord;

import base.messages.*;
import base.channel.*;
import base.Peer;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;

import static base.Clauses.createSocket;

public class TCPMessageHandler implements ChordMessageHandler {
    @Override
    public SSLSocket sendFindSuccessorRequest(ChordIdentifier id, InetSocketAddress peerAddress) throws NoResponse {
        SSLSocket socket;
        try {
            socket = createSocket(peerAddress);
        } catch (IOException e) {
            throw new NoResponse();
        }
        Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createFindSuccessorMessage(id.getIdentifier())));

        return socket;
    }

    @Override
    public boolean pingPeer(InetSocketAddress peerAddress) {

        SSLSocket socket;
        try {
            socket = createSocket(peerAddress);
        } catch (IOException e) {
            return false;
        }
        Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.dummyMessage(), true));

        return true;
    }

    @Override
    public SSLSocket sendGetPredecessorRequest(InetSocketAddress peerAddress) throws NoResponse {
        SSLSocket socket;
        try {
            socket = createSocket(peerAddress);
        } catch (IOException e) {
            throw new NoResponse();
        }
        Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createGetPredecessorMessage()));

        return socket;
    }

    @Override
    public void sendDisconnectToSuccessor(InetSocketAddress successorAddress, ChordIdentifier predecessor) {
        SSLSocket socket;
        try {
            socket = createSocket(successorAddress);
        } catch (IOException e) {
            return;
        }
        Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createSendDisconnectToSuccessorMessage(predecessor), true));
    }

    @Override
    public void sendDisconnectToPredecessor(InetSocketAddress predecessorAddress, ChordIdentifier newSuccessor) {
        SSLSocket socket;
        try {
            socket = createSocket(predecessorAddress);
        } catch (IOException e) {
            return;
        }
        Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createSendDisconnectToPredecessorMessage(newSuccessor), true));
    }

    @Override
    public SSLSocket sendGetSuccessorList(InetSocketAddress peerAddress, boolean allSuccessors) throws NoResponse {
        SSLSocket socket;
        try {
            socket = createSocket(peerAddress);
        } catch (IOException e) {
            throw new NoResponse();
        }
        if(allSuccessors) Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createGetAllSuccessorsMessage()));
        else Peer.getTaskManager().execute(new MessageSender(socket, ChordMessage.createGetSuccessorListMessage()));

        return socket;
    }

    @Override
    public void listenToReply(SSLSocket socket) {
        Peer.getTaskManager().execute(new MessageReceiver(socket));
    }
}
