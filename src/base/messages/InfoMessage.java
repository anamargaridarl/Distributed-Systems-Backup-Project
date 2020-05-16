package base.messages;

import base.Clauses;

import static base.Clauses.CRLF;

public class InfoMessage {

    protected String address;
    protected int port;

    public InfoMessage(String address, int port) {
        this.address=address;
        this.port = port;
    }

    public InfoMessage(String[] msg) {
        this.address = msg[0];
        this.port = Integer.parseInt(msg[1]);
    }


    public String createMessage() {
        String response;
        response = address + " " + port;
        return response;
    }

    public String createMessageFinal() {
        String response;
        response = createMessage() + CRLF + CRLF;
        return response;
    }

    public byte[] toByteArray() {
        return this.createMessage().getBytes();
    }

    public byte[] toByteArrayFinal() {
        return this.createMessageFinal().getBytes();
    }
}
