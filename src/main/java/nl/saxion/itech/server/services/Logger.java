package nl.saxion.itech.server.services;

public class Logger {
    private ClientHandler clientHandler;

    private Logger(ClientHandler clientHandler) {
        this.clientHandler = ClientHandler.getInstance();
    }



    public void stats() {
        System.out.printf("Total number of clients: %d\n", clientHandler.getClients().size());
//        int connected = clients.filter(c => c.status == STAT_CONNECTED)
//        long connected = clients.stream().filter(ClientThread::isConnected).count();
//        System.out.printf("Total number of connected clients: %d\n", connected);
    }
}
