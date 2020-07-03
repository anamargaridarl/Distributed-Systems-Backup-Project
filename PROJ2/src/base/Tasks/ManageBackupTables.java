package base.Tasks;

import base.Peer;
import base.PeerLogger;
import base.channel.MessageSender;
import base.messages.BackupTablesMessage;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

import static base.Clauses.createSocket;

public class ManageBackupTables implements Runnable {

    @Override
    public void run() {
        try {
            SSLSocket succSocket = createSocket(Peer.getChordManager().getSuccessor().getOwnerAddress());
            BackupTablesMessage bt_message = new BackupTablesMessage();
            Peer.getTaskManager().execute(new MessageSender(succSocket, bt_message));
        } catch (IOException e) {
            PeerLogger.channelsDisrupt();
        }
    }
}
