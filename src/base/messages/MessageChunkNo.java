package base.messages;

import base.Clauses;
import base.Peer;

import java.net.InetSocketAddress;

public class MessageChunkNo extends Message {

    protected int number;
    protected InetSocketAddress origin;

    public MessageChunkNo(String v, String type, int sid, String fid, int number) {
        super(v, type, sid, fid);
        this.number = number;
        origin = new InetSocketAddress(Peer.getServerPort());
    }

    public MessageChunkNo(String v, String type, int sid, String fid, int number, InetSocketAddress peerAddr) {
        super(v, type, sid, fid);
        this.number = number;
        origin = peerAddr;
    }

    public MessageChunkNo(String[] message) {
        super(message[0], message[1], Integer.parseInt(message[2]), message[3]);
        number = Integer.parseInt(message[4]);
        origin = null;
    }

    public int getNumber() {
        return number;
    }

    public InetSocketAddress getOrigin() {
        return origin;
    }

    public void setOrigin(InetSocketAddress origin) {this.origin = origin;}

    public String createMessageFinal() {
        String super_msg = super.createMessage() + " " + this.number + Clauses.CRLF + Clauses.CRLF;
        return super_msg;
    }

    @Override
    public String createMessage() {
        String response;
        response = super.createMessage() + " " + this.number;
        return response;
    }

    @Override
    public byte[] toByteArray() {
        return this.createMessage().getBytes();
    }

    @Override
    public byte[] toByteArrayFinal() {
        return this.createMessageFinal().getBytes();
    }
}
