package base.messages;

import base.Clauses;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TablesToSendMessage extends BaseMessage {
    private final ConcurrentHashMap<String, InetSocketAddress> initiatorsToSend;
    private final ConcurrentHashMap<String, Set<InetSocketAddress>> successorsToSend;

    public TablesToSendMessage(ConcurrentHashMap<String, InetSocketAddress> initiatorsToSend, ConcurrentHashMap<String, Set<InetSocketAddress>> successorsToSend) {
        super(Clauses.NEW_TABLES, 0);
        this.initiatorsToSend = initiatorsToSend;
        this.successorsToSend = successorsToSend;
    }

    public ConcurrentHashMap<String, InetSocketAddress> getInitiatorsToSend() {
        return initiatorsToSend;
    }

    public ConcurrentHashMap<String, Set<InetSocketAddress>> getSuccessorsToSend() {
        return successorsToSend;
    }
}
