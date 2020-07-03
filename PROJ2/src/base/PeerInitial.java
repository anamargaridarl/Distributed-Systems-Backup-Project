package base;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PeerInitial {

    static int s_id;

    static Registry registry;
    static String remote_name;

    static int port;

    static String knownPeerIP;
    static int knownPeerPort;

    static Peer obj;

    public static void main(String[] args) throws IOException {
        if (args.length != 3 && args.length != 5) {
            System.out.println("Invalid arguments. Please use: java PeerInitial <peer_id> <remote_name> <port>\nOr java PeerInitial <peer_id> <remote_name> <port> <knownPeerIP> <knownPeerPort>");
            System.exit(1);
        }

        s_id = Integer.parseInt(args[0]);
        remote_name = args[1];
        port = Integer.parseInt(args[2]);

        if (args.length == 5) {
            knownPeerIP = args[3];
            knownPeerPort = Integer.parseInt(args[4]);
            obj = new Peer(s_id, port, knownPeerIP, knownPeerPort);
        } else obj = new Peer(s_id, port);

        open();
    }

    public static void open() throws RemoteException {

        try {
            registry = LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            System.out.println("Registry already activate. Won't create new one.");
            registry = LocateRegistry.getRegistry();
        }


        try {
            registry.rebind(remote_name, obj);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Server ready");
    }


    public void close() {
        try {
            registry.unbind(remote_name);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
