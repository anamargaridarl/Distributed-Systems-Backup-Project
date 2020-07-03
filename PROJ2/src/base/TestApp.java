package base;

import java.io.DataOutput;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class TestApp {
    static final int SUCCESS = 0;
    static final int ERROR = -1;
    private static PeerInterface backup_service;

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Not enough arguments inserted. Please use this format:\njava TestApp <peer_ap> <operation> <opnd_1> <opnd_2>");
            System.exit(ERROR);
        }

        //Retrieve information to access remote object of the Server
        String peer_ap = args[0];
        String[] operation = retrieveOperation(args);

        try {
            //Access the remote object and process the request
            Registry registry = LocateRegistry.getRegistry();
            backup_service = (PeerInterface) registry.lookup(peer_ap);
            processRequest(operation);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
        }
    }

    private static String[] retrieveOperation(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    private static void processRequest(String[] operands) throws RemoteException {
        switch (operands[0].toUpperCase()) {
            case "BACKUP":
                processBackup(operands);
                break;
            case "RESTORE":
                processRestore(operands);
                break;
            case "DELETE":
                processDelete(operands);
                break;
            case "RECLAIM":
                processReclaim(operands);
                break;
            case "STATE":
                processState(operands);
                break;
            default:
                System.err.println("Invalid Operation. Please one of the follow: BACKUP, RESTORE, DELETE, RECLAIM or STATE");
                System.exit(-1);
        }
    }

    private static void processBackup(String[] operands) throws RemoteException {

        if (operands.length != 3) {
            System.err.println("Invalid backup operation. Please use as follows: BACKUP <filepath> <replication_degree>");
            System.exit(ERROR);
        }

        int op_status = backup_service.backup(operands[1], Integer.parseInt(operands[2]));
        if (op_status == SUCCESS) {
            System.out.println("File was successfully backed up.");
            System.exit(SUCCESS);
        } else if (op_status == ERROR) {
            System.err.println("Service was unable to back up the file.");
            System.exit(ERROR);
        }
    }

    private static void processDelete(String[] operands) {
        if (operands.length != 2) {
            System.err.println("Invalid delete operation. Please use as follows: DELETE <filepath>");
            System.exit(ERROR);
        }

        try {
            int op_status = backup_service.delete(operands[1]);
            if (op_status == SUCCESS) {
                System.out.println("File was successfully deleted.");
                System.exit(SUCCESS);
            } else if (op_status == ERROR) {
                System.err.println("Service was unable to delete the file.");
                System.exit(ERROR);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void processRestore(String[] operands) {
        if (operands.length != 2) {
            System.err.println("Invalid restore operation. Please use as follows: RESTORE <filepath>");
            System.exit(ERROR);
        }

        try {
            int op_status = backup_service.restore(operands[1]);
            if (op_status == SUCCESS) {
                System.out.println("Processed operation.");
                System.exit(SUCCESS);
            } else if (op_status == ERROR) {
                System.err.println("Service was unable to restore the file.");
                System.exit(ERROR);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void processReclaim(String[] operands) {
        if (operands.length != 2) {
            System.err.println("Invalid delete operation. Please use as follows: RECLAIM <max_space>");
            System.exit(ERROR);
        }

        try {
            int op_status = backup_service.reclaim(Integer.parseInt(operands[1]));
            if (op_status == SUCCESS) {
                System.out.println("Space was successfully reclaimed.");
                System.exit(SUCCESS);
            } else if (op_status == ERROR) {
                System.err.println("Service was unable to reclaim the specified space.");
                System.exit(ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processState(String[] operands) {
        if (operands.length != 1) {
            System.err.println("Invalid delete operation. Please use as follows: STATE");
            System.exit(ERROR);
        }

        try {
            List<String> state = backup_service.state();
            for (String line : state) {
                System.out.println(line);
            }
            System.exit(SUCCESS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
