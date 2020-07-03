package base.Tasks;

import base.Peer;

import java.util.concurrent.Callable;

public class CheckActualRepDeg implements Callable<Boolean> {
    private final String file_id;
    private final int number;
    private final int rep_deg;

    public CheckActualRepDeg(String file_id, int number, int rep_deg) {
        this.file_id = file_id;
        this.number = number;
        this.rep_deg = rep_deg;
    }

    @Override
    public Boolean call() throws Exception {
        return rep_deg <= Peer.getStorageManager().getStoredSendersOccurrences(file_id, number);
    }
}
