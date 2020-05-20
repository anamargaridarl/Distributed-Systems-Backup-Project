package base.messages;

import base.Clauses;

import java.net.InetSocketAddress;

import static base.Clauses.CRLF;

public class InfoMessage extends MessageChunkNo {

    protected InetSocketAddress address;

    public InfoMessage(String version, String type, int sender_id, String file_id, int num, InetSocketAddress address) {
        super(version,type,sender_id,file_id,num);
        this.address=address;
    }

    public InetSocketAddress getAddress()
    {
        return address;
    }



}
