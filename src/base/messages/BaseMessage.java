package base.messages;

import base.Clauses;

import java.io.Serializable;

import static base.Clauses.CRLF;

public class BaseMessage implements Serializable {

    protected int SenderId;
    protected String type;

    public BaseMessage(String ty, int sid) {
        SenderId = sid;
        type = ty;
    }

    public BaseMessage(String[] msg) {
        type = msg[0];
        SenderId = Integer.parseInt(msg[1]);
    }

    public int getSenderId() {
        return SenderId;
    }


    public String getType() {
        return type;
    }


}
