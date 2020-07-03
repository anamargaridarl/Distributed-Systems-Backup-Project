package base.chord;

import javax.net.ssl.SSLSocket;
import java.net.InetSocketAddress;

public interface ChordMessageHandler {
    //In the future to send messages I should do Peer.getTaskManager().execute(new MessageSender(createSocket..., message we want to send));

    public SSLSocket sendFindSuccessorRequest(ChordIdentifier id, InetSocketAddress peerAddress) throws NoResponse;
    /* Mensagens necessárias ao implementar

        ChordMessage msg = new ChordMessage(version, "FIND_SUCC", sender_id, id);

        -------------------------------------------------------------------------------------

        ChordIdentifier / InetSocketAddress succ_identifier = chordManager.findSuccessor(id);
        ChordReply reply = new ChordReply(version, "SUCC", sender_id, succ_identifier);

    */

    public boolean pingPeer(InetSocketAddress peerAddress); //True if peer is online
    /* Mensagens necessárias ao implementar

        BaseMessage msg = new BaseMessage(version, "PING", sender_id);

        -------------------------------------------------------------------------------------

        BaseMessage reply = new BaseMessage(version, "PING_RCV", sender_id, );

    */

    public SSLSocket sendGetPredecessorRequest(InetSocketAddress peerAddress) throws NoResponse;
    /* Mensagens necessárias ao implementar

        BaseMessage msg = new BaseMessage(version, "FIND_PRED", sender_id);

        -------------------------------------------------------------------------------------

        ChordIdentifier / InetSocketAddress pred_identifier = ...
        ChordReply reply = new ChordReply(version, "pred", sender_id, pred_identifier);

    */

    public void sendDisconnectToSuccessor(InetSocketAddress successorAddress, ChordIdentifier predecessor);
    /* Mensagens necessárias ao implementar

        ChordReply msg = new ChordReply(version, "DISC_SUCC", sender_id, predecessor);

    */

    public void sendDisconnectToPredecessor(InetSocketAddress predecessorAddress, ChordIdentifier newSuccessor);
    /* Mensagens necessárias ao implementar

        ChordReply msg = new ChordReply(version, "DISC_PRED", sender_id, newSuccessor);

    */

    public SSLSocket sendGetSuccessorList(InetSocketAddress peerAddress, boolean allSuccessors) throws NoResponse;

    public void listenToReply(SSLSocket socket);
}
