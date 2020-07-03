package base.Tasks;

import base.Peer;
import base.messages.BackupTablesMessage;

public class HandleBackupTables implements Runnable {
  private final BackupTablesMessage bt_message;

  public HandleBackupTables(BackupTablesMessage bt_message) {
    this.bt_message = bt_message;
  }

  @Override
  public void run() {
    Peer.getStorageManager().setBckupStoredSenders(bt_message.getBckupStoredSenders());
    Peer.getStorageManager().setBckupSuccessorsStoredSenders(bt_message.getBckupSuccSenders());
    Peer.getStorageManager().setBckupInitiators(bt_message.getBckupInitiators());
  }
}
