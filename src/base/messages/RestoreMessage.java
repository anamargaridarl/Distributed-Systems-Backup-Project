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

    public void setBody(byte[] body) {
        this.body = body;
    }


    public byte[] getBody() {
        return body;
    }

    public int getNumChunks() {
        return this.number_chunks;
    }

    public byte[] createByteModifiedMessage(Integer port) throws IOException {
        byte[] msg = createMessageFinal().getBytes();
        ByteArrayOutputStream add_arrays = new ByteArrayOutputStream();
        add_arrays.write(msg);
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(port);
        add_arrays.write(bb.array());
        return add_arrays.toByteArray();
    }

    public byte[] createByteMessage() throws IOException {
        String msg = createMessage() + " " + this.number_chunks + Clauses.CRLF + Clauses.CRLF;
        byte[] msg_bytes = msg.getBytes();
        ByteArrayOutputStream add_arrays = new ByteArrayOutputStream();
        add_arrays.write(msg_bytes);
        add_arrays.write(body);
        return add_arrays.toByteArray();
    }
}
