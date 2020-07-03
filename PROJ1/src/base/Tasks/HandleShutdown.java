package base.Tasks;

import base.Peer;
import base.TaskLogger;

import java.util.concurrent.TimeUnit;

import static base.Clauses.TIMEOUT;

public class HandleShutdown extends Thread {

    @Override
    public void run() {
        try {
            TaskLogger.startShutdown(TIMEOUT);
            Peer.getTaskManager().execute(new SaveState());
            Peer.getTaskManager().shutdown();
            Peer.getTaskManager().awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
            TaskLogger.shutdownOk();
        } catch (InterruptedException e) {
            TaskLogger.forcedShutdown();
        }
    }
}
