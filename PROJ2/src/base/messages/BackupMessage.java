package base.messages;

public class BackupMessage extends MessageChunkNo {

    protected int replicationDeg;
    protected int n_chunks;
    protected byte[] chunk;

    public BackupMessage(String type, int sid, String fid, int chunkid, int repd, int nchunks, byte[] bdy) {
        super(type, sid, fid, chunkid);
        replicationDeg = repd;
        chunk = bdy;
        n_chunks = nchunks;
    }

    public Integer getReplicationDeg() {
        return replicationDeg;
    }

    public byte[] getBody() {
        return chunk;
    }

    public int getNumberChunks() {
        return this.n_chunks;
    }
}
