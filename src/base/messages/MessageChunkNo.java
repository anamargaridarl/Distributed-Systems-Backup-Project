package base.messages;

import base.Clauses;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static base.Clauses.CR;
import static base.Clauses.LF;

public class MessageChunkNo extends Message {

    protected int number;

    public MessageChunkNo(String v, String type, int sid, String fid, int number) {
        super(v, type, sid, fid);
        this.number = number;
    }

    public MessageChunkNo(String[] message) {
        super(message[0], message[1], Integer.parseInt(message[2]), message[3]);
        number = Integer.parseInt(message[4]);
    }

    public int getNumber() {
        return number;
    }

    public String createMessageFinal() {
        String super_msg = super.createMessage() + " " + this.number + " " + Clauses.CRLF + Clauses.CRLF;
        return super_msg;
    }

    @Override
    public String createMessage() {
        String response;
        response = super.createMessage() + " " + this.number;
        return response;
    }


}
