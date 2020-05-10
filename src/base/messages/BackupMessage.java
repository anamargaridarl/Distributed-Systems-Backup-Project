package base.messages;

import base.Clauses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static base.Clauses.CR;
import static base.Clauses.LF;

public class BackupMessage extends MessageChunkNo {

    protected int replicationDeg;
    protected byte[] chunk;

    public BackupMessage(String v, String type, int sid, String fid, int chunkid, int repd, byte[] bdy) {
        super(v, type, sid, fid, chunkid);
        replicationDeg = repd;
        chunk = bdy;
    }

    //boolean used to distinguish send and reply messages
    public BackupMessage(String[] header, byte[] body) {
        super(header[0], header[1], Integer.parseInt(header[2]), header[3], Integer.parseInt(header[4]));
        replicationDeg = Integer.parseInt(header[5]);
        chunk = body;
    }

    public Integer getReplicationDeg() {
        return replicationDeg;
    }

    public byte[] getBody() {
        return chunk;
    }

    public byte[] createByteMessage() throws IOException {
        byte[] response;
        String super_msg = super.createMessage() + " " + replicationDeg + Clauses.CRLF + Clauses.CRLF;
        response = super_msg.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(response);
        baos.write(chunk);
        response = baos.toByteArray();
        return response;
    }

}
