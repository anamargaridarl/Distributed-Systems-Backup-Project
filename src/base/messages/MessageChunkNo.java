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

    public static List<byte[]> separateHeaderAndBody(byte[] message) {
        int i = 0;
        for (; i < message.length - 4; i++) { //check the message where the <CRLF><CRLF> are located
            if (message[i] == CR && message[i + 1] == LF && message[i + 2] == CR && message[i + 3] == LF)
                break;
        }
        if (i == message.length - 4) {
            return null;
        }
        List<byte[]> newList = new ArrayList<>();
        byte[] header = Arrays.copyOfRange(message, 0, i);
        byte[] body = Arrays.copyOfRange(message, i + 4, message.length);
        newList.add(header);
        newList.add(body);
        return newList;
    }

    public static String[] parseHeader(byte[] header) {
        return new String(header, 0, header.length).split(" ");
    }


}
