package base.messages;

import base.Clauses;

import static base.Clauses.CRLF;

public class BaseMessage {

    protected String version;
    protected int SenderId;
    protected String type;

    public BaseMessage(String v, String ty, int sid) {
        version = v;
        SenderId = sid;
        type = ty;
    }

    public BaseMessage(String[] msg) {
        version = msg[0];
        type = msg[1];
        SenderId = Integer.parseInt(msg[2]);
    }

    public int getSenderId() {
        return SenderId;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String createMessage() {
        String response;
        response = version + " " + type + " " + SenderId + " ";
        return response;
    }

    public String createMessageFinal() {
        String response;
        response = createMessage() + CRLF + CRLF;
        return response;
    }

}
