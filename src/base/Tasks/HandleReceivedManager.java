package base.Tasks;

import base.Peer;
import base.TaskLogger;
import base.messages.*;

import java.io.IOException;
import java.net.Socket;

import static base.Clauses.*;

/*
    Class that manages incoming messages sent through multicast channels
*/
public class HandleReceivedManager implements Runnable {
    private final Socket client_socket;
    private final Object msg;
    private final String type;

    public HandleReceivedManager(Object msg, Socket c_socket) {
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
                case DECLINED: //TODO: check if any handling is necessary with this message
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
                case ASKDELETE:
                    handleAskDelete();
                    break;
                case REMOVED:
                    handleRemovedChunk();
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
                default:
                    TaskLogger.invalidMessage(type);
                    client_socket.close();
                    break;
            }
        } catch (NumberFormatException e) {
            TaskLogger.invalidSenderID(NOT_INITIATOR);
        } catch (IOException e) {
            e.printStackTrace(); //log error
        }
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

    private void handleAskDelete() {
        BaseMessage deleteReply = (BaseMessage) msg;
        Peer.getTaskManager().execute(new HandleDeleteOffline(deleteReply, client_socket));
    }

    private void handleRemovedChunk() {
        MessageChunkNo removedChunk = (MessageChunkNo) msg;
        Peer.getTaskManager().execute(new HandleRemovedChunk(removedChunk));
    }
}

