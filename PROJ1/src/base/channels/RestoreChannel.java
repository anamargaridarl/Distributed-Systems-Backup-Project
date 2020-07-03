package base.channels;

import base.Peer;
import base.Tasks.HandleChunk;
import base.Tasks.HandleReceivedManager;
import base.messages.BackupMessage;
import base.messages.MessageChunkNo;
import base.messages.RestoreMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.List;

public class RestoreChannel extends Channel {

    public RestoreChannel(String mdr_addr, int mdr_port) throws IOException {
        super(mdr_addr, mdr_port);
    }

    @Override
    protected void parseMessage(DatagramPacket packet) throws IOException {
        byte[] buf = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        List<byte[]> message_parts = MessageChunkNo.separateHeaderAndBody(buf);
        String[] header_args = MessageChunkNo.parseHeader(message_parts.get(0));
        Peer.getTaskManager().execute(new HandleChunk(header_args, message_parts.get(1), packet.getAddress()));
    }
}
