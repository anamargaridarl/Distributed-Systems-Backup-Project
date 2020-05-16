package base.messages;

import base.Clauses;

import static base.Clauses.CRLF;

public class SucInfoMessage {

    int flag;

    public SucInfoMessage(int flag) {
        this.flag = flag;
    }

    public SucInfoMessage(String[] msg) {
        this.flag = Integer.parseInt(msg[0]);
    }

    public int getFlag() {
        return flag;
    }

    public String createMessage() {
        return String.valueOf(flag);
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
