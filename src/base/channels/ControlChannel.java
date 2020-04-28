package base.channels;

import base.Peer;
import base.Tasks.HandlePutChunk;
import base.Tasks.HandleReceivedManager;
import base.messages.BackupMessage;

import java.io.IOException;
import java.net.DatagramPacket;

public class ControlChannel extends Channel {

    public ControlChannel(String mc_addr, int mc_port) throws IOException {
        super(mc_addr, mc_port);
    }

    @Override
    protected void parseMessage(DatagramPacket packet) throws IOException {
        byte[] buf = packet.getData();
        String[] message = new String(buf, 0, packet.getLength()).split("\\s+");
        Peer.getTaskManager().execute(new HandleReceivedManager(message));
    }
}
