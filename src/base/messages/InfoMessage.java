package base.messages;

import base.Clauses;

import static base.Clauses.CRLF;

public class InfoMessage extends BaseMessage {

    protected String address;
    protected int port;

    public InfoMessage(String address, int port) {
        super("1.0", "GETTOIDEAL" ,1);
        this.address=address;
        this.port = port;
    }

    public InfoMessage(String[] msg) {
        super(msg[0],msg[1], Integer.parseInt(msg[2]));
        this.address = msg[3];
        this.port = Integer.parseInt(msg[4]);
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
