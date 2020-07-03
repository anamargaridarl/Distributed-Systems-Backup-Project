package base.Tasks;

import base.Storage.StorageManager;

public class SaveState implements Runnable {
    @Override
    public void run() {
        StorageManager.saveStorageManager();
    }
}
