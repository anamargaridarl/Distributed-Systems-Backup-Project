package base.messages;

import base.Clauses;

import static base.Clauses.CRLF;

public class InfoMessage extends MessageChunkNo {

    protected String address;
    protected int port;

    public InfoMessage(String version, String type, int sender_id, String file_id, int num, String address, int port) {
        super(version,type,sender_id,file_id,num);
        this.address=address;
        this.port = port;
    }

    public InfoMessage(String[] msg) {
        super(msg[0],msg[1],Integer.parseInt(msg[2]),msg[3],Integer.parseInt(msg[4]));
        this.address = msg[5];
        this.port = Integer.parseInt(msg[6]);
    }


    public String createMessage() {
        String response;
        response = super.createMessage() + " " + address + " " + port;
        return response;
    }

    public String createMessageFinal() {
        String response;
        response = createMessage()  + CRLF + CRLF;
        return response;
    }

    public byte[] toByteArray() {
        return this.createMessage().getBytes();
    }

    public byte[] toByteArrayFinal() {
        return this.createMessageFinal().getBytes();
    }
}
