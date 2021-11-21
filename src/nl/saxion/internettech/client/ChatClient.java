package nl.saxion.internettech.client;

import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private final Thread readThread;
    private final Thread writeThread;

    public ChatClient(String serverAddress, int port) {
        try {
            Socket socket = new Socket(serverAddress, port);
            this.readThread = new Thread(new MessageWriter(socket));
            this.writeThread = new Thread(new MessageReader(socket));
        } catch (IOException e) {
            throw new Error("I/O Error: " + e.getMessage());
        }
    }

    public void start() {
        this.readThread.start();
        this.writeThread.start();
    }

    public static void showMenu() {
        System.out.print(
                """
                        CONN \t\t Login to the server with a username
                        BCST \t\t Broadcast a message to every client on the server
                        PONG \t\t Reply to the server's PING request
                        QUIT \t\t Close connection with the server
                        
                        """);
    }
}
