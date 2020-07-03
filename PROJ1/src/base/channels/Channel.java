package base.channels;

import base.ChannelLogger;

import java.io.IOException;
import java.net.*;

import static base.Clauses.MAX_SIZE;

public abstract class Channel implements Runnable {

    MulticastSocket socket = null;
    String m_addr;
    int m_port;
    private static byte[] buf;
    DatagramPacket packet;

    Channel(String addr, int port) throws IOException {
        m_addr = addr;
        m_port = port;
        socket = new MulticastSocket(m_port);
        openChannel();
    }

    public void openChannel() throws IOException {
        socket.joinGroup(InetAddress.getByName(m_addr));
    }

    public void sendMessage(byte[] message) throws UnknownHostException {
        buf = new byte[MAX_SIZE + 100];
        buf = message;
        packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(m_addr), m_port);

        try {
            socket.send(packet);
        } catch (IOException e) {
            ChannelLogger.messageSendFail();
        }
    }

    protected abstract void parseMessage(DatagramPacket packet) throws IOException;

    @Override
    public void run() {
        while (true) {
            buf = new byte[MAX_SIZE + 100]; //fit header and chunk size
            packet = new DatagramPacket(buf, buf.length);
            try {
                buf = new byte[MAX_SIZE + 100]; //fit header and chunk size
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                parseMessage(packet);
            } catch (IOException e) {
                ChannelLogger.packetParseFail();
            }
        }

    }


}
