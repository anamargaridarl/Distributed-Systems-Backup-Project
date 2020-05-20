package base.messages;

import base.Clauses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;

public class RestoreMessage extends MessageChunkNo {

    byte[] body;
    int number_chunks;

    public RestoreMessage(String v, String type, int sid, String fid, int number, int number_chunks, byte[] bdy) {
        super(v, type, sid, fid, number);
        body = bdy;
        this.number_chunks = number_chunks;
    }

    public RestoreMessage(String[] message, byte[] body) {
        super(message[0], message[1], Integer.parseInt(message[2]), message[3], Integer.parseInt(message[4]));
        this.number_chunks =  Integer.parseInt(message[5]);
        this.body = body;
    }


    public byte[] getBody() {
        return body;
    }

    public int getNumChunks() {
        return this.number_chunks;
    }

}
