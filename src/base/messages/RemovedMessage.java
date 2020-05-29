package base.messages;

import base.ChunkInfo;

public class RemovedMessage extends MessageChunkNo {

    private final ChunkInfo chunk;

    public RemovedMessage(String type, int sid, String fid, int number, ChunkInfo chunk) {
        super(type, sid, fid, number);
        this.chunk = chunk;
    }

    public ChunkInfo getChunkInfo() {
        return chunk;
    }
}
