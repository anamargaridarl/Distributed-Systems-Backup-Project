package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.chord.ChordIdentifier;
import base.messages.*;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static base.Clauses.*;

/*
    Class that manages incoming messages sent through multicast channels
*/
public class HandleReceivedManager implements Runnable {
    private final SSLSocket client_socket;
    private final Object msg;
    private final String type;

    public HandleReceivedManager(Object msg, SSLSocket c_socket) {
        this.msg = msg;
        this.type = ((BaseMessage) msg).getType();
        this.client_socket = c_socket;
    }

    @Override
    public void run() {
        try {
            switch (type) {
                case PUTCHUNK:
                    handlePutChunk();
                    break;
                case STORED:
                    handleStored();
                    break;
                case DECLINED:
                    handleDeclined();
                    break;
                case GETCHUNK:
                    handleGetChunk();
                    break;
                case CHUNK:
                    handleChunk();
                    break;
                case DELETE:
                    handleDeleteFile();
                    break;
                case REMOVED:
                    handleRemovedChunk();
                    break;
                case SUCCGETCHUNK:
                    handleSuccGetChunk();
                    break;
                case NUMREPLY:
                    handleNumDeleteReply();
                    break;
                case FORWARDGET:
                    handleForwardGet();
                    break;
                case GETTOIDEAL:
                    handleGetToIdeal();
                    break;
                case REPLYINFOINITIATOR:
                    handleReplyInfoInitiator();
                    break;
                case GET_SUCCESSOR_LIST:
                    handleGetSuccessorList();
                    break;
                case GET_ALL_SUCCESSORS:
                    handleGetAllSuccList();
                    break;
                case SUCC_LIST:
                    handleReceivedSuccessorList();
                    break;
                case ALL_SUCC:
                    handleReceivedListOfAllSuccessors();
                    break;
                case GET_PREDECESSOR:
                    handleGetPredecessor();
                    break;
                case PREDECESSOR:
                    handlePredecessorReceived();
                    break;
                case FIND_SUCCESSOR:
                    handleFindSuccessor();
                    break;
                case SUCCESSOR:
                    handleReceivedSuccessor();
                    break;
                case SUCCESSOR_DISCONNECT:
                    handleReceivedSuccessorDisconnect();
                    break;
                case PREDECESSOR_DISCONNECT:
                    handleReceivedPredecessorDisconnect();
                    break;
                case DUMMY: //Dummy messages don't need handling
                    client_socket.close();
                    break;
                case BACKUPTABLES:
                    handleBackupTables();
                    break;
                case NEW_TABLES:
                    handleReceivedNewTables();
                    break;
                default:
                    TaskLogger.invalidMessage(type);
                    client_socket.close();
                    break;
            }
        } catch (NumberFormatException e) {
            TaskLogger.invalidSenderID(NOT_INITIATOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedNewTables() {
        TablesToSendMessage tableMsg = (TablesToSendMessage) msg;
        Peer.getTaskManager().execute(new HandleReceivedTables(tableMsg.getInitiatorsToSend(), tableMsg.getSuccessorsToSend()));
    }


    private void handleReceivedPredecessorDisconnect() {
        ChordDisconnectMsg message = (ChordDisconnectMsg) msg;
        ChordIdentifier newPredecessor = message.getPeerIdentifier();
        Peer.getChordManager().changePredecessorOnDisconnect(message.getSenderIdentifier(), newPredecessor);

        try {
            client_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedSuccessorDisconnect() {
        ChordDisconnectMsg message = (ChordDisconnectMsg) msg;
        ChordIdentifier newSuccessor = message.getPeerIdentifier();
        Peer.getChordManager().changeSuccessorOnDisconnect(message.getSenderIdentifier(), newSuccessor);

        try {
            client_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedSuccessor() {
        ChordIdentifierMessage message = (ChordIdentifierMessage) msg;
        ChordIdentifier successor = message.getPeerIdentifier();
        Peer.getChordManager().addSuccessorToReplyTable(message.getSenderID().getIdentifier(), successor); //stores the reply so it may be accessed by other threads
        Peer.getTaskManager().schedule(new RemoveEntryFromReplyTable(message.getSenderID().getIdentifier(), successor), WAIT_FOR_REPLY * (MAX_RETRIES + 1), TimeUnit.MILLISECONDS); //Deletes the entry in case it wasn't looked up

        try {
            client_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFindSuccessor() {
        ChordMessage message = (ChordMessage) msg;
        Peer.getTaskManager().execute(new HandleFindSuccessor(message, client_socket));
    }

    private void handlePredecessorReceived() {
        ChordIdentifierMessage message = (ChordIdentifierMessage) msg;
        ChordIdentifier predecessor = message.getPeerIdentifier();
        Peer.getChordManager().stabilize(predecessor);

        try {
            client_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleGetPredecessor() {
        ChordIdentifierMessage message = (ChordIdentifierMessage) msg;
        ChordIdentifier sender = message.getSenderID();
        Peer.getTaskManager().execute(new HandleGetPredecessor(client_socket, sender));
    }

    private void handleReceivedSuccessorList() {
        ChordReplyArray message = (ChordReplyArray) msg;
        ChordIdentifier[] list = message.getSuccList();
        Peer.getChordManager().fixSuccessorList(list);

        try {
            client_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedListOfAllSuccessors() {
        ChordReplyArray message = (ChordReplyArray) msg;
        ChordIdentifier[] list = message.getSuccList();
        Peer.getChordManager().addSuccessorListToReplyTable(message.getSender(), list);
        Peer.getTaskManager().schedule(new RemoveEntryFromAllSuccTable(message.getSender(), list), WAIT_FOR_REPLY * (MAX_RETRIES + 1), TimeUnit.MILLISECONDS); //Deletes the entry in case it wasn't looked up

        try {
            client_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetSuccessorList() {
        Peer.getTaskManager().execute(new HandleSendSuccessorList(client_socket));
    }

    private void handleGetAllSuccList() {
        Peer.getTaskManager().execute(new HandleSendSuccessorList(client_socket, true));
    }

    private void handleReplyInfoInitiator() {
        InfoMessage infoinitiator = (InfoMessage) msg;
        Peer.getTaskManager().execute(new HandleInfoToInitiator(infoinitiator));
    }


    private void handleGetToIdeal() {
        StatePeerMessage infoinitiator = (StatePeerMessage) msg;
        Peer.getTaskManager().execute(new HandleGetToIdeal(infoinitiator, client_socket));
    }


    private void handleForwardGet() {
        MessageChunkNo infoinitiator = (MessageChunkNo) msg;
        Peer.getTaskManager().execute(new HandleForwardGet(infoinitiator, client_socket));
    }


    private void handleNumDeleteReply() {
        ChunkReplyMessage numReply = (ChunkReplyMessage) msg;
        Peer.getTaskManager().execute(new HandleNumDeleteReply(numReply));
    }


    private void handlePutChunk() {
        BackupMessage bckup = (BackupMessage) msg;
        Peer.getTaskManager().execute(new HandlePutChunk(bckup, client_socket));
    }

    private void handleStored() {
        MessageChunkNo stored = (MessageChunkNo) msg;
        Peer.getTaskManager().execute(new HandleStored(stored));
    }

    private void handleDeclined() {
        MessageChunkNo declined = (MessageChunkNo) msg;
        Peer.getTaskManager().execute(new HandleDeclined(declined));
    }

    private void handleGetChunk() {
        MessageChunkNo getChunk = (MessageChunkNo) msg;
        Peer.getTaskManager().execute(new HandleGetChunk(getChunk, client_socket));
    }

    private void handleChunk() {
        RestoreMessage restore = (RestoreMessage) msg;
        Peer.getTaskManager().execute(new HandleChunk(restore));
    }

    private void handleDeleteFile() {
        MessageChunkNo deleteFile = (MessageChunkNo) msg;
        Peer.getTaskManager().execute(new HandleDeleteFile(deleteFile, client_socket));
    }

    private void handleRemovedChunk() {
        RemovedMessage removedChunk = (RemovedMessage) msg;
        Peer.getTaskManager().execute(new HandleRemovedChunk(removedChunk));
    }

    private void handleSuccGetChunk() {
        MessageChunkNo succGetChunk = (MessageChunkNo) msg;
        Peer.getTaskManager().execute(new HandleSuccGetChunk(succGetChunk, client_socket));
    }

    private void handleBackupTables() {
        BackupTablesMessage bt_message = (BackupTablesMessage) msg;
        Peer.getTaskManager().execute(new HandleBackupTables(bt_message));
    }
}

