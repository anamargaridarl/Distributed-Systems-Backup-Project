package base.messages;

import base.Peer;

import java.net.InetSocketAddress;

public class MessageChunkNo extends Message {

    protected int number;
    protected InetSocketAddress origin;

    public MessageChunkNo(String type, int sid, String fid, int number) {
        super(type, sid, fid);
        this.number = number;
        origin = new InetSocketAddress(Peer.getServerPort());
    }

    public MessageChunkNo(String v, String type, int sid, String fid, int number, InetSocketAddress peerAddr) {
        super(type, sid, fid);
        this.number = number;
        origin = peerAddr;
    }

    public int getNumber() {
        return number;
    }

    public InetSocketAddress getOrigin() {
        return origin;
    }

    public void setOrigin(InetSocketAddress origin) {
        this.origin = origin;
    }


}
