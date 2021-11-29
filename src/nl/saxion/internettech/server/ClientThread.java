package nl.saxion.internettech.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected;
    private String username;
    private boolean receivedPong;

    protected static final String CMD_CONN = "CONN";
    protected static final String CMD_BCST = "BCST";
    protected static final String CMD_OK = "OK";
    protected static final String CMD_INFO = "INFO";
    protected static final String CMD_PING = "PING";
    protected static final String CMD_PONG = "PONG";
    protected static final String CMD_QUIT = "QUIT";
    protected static final String CMD_ER00 = "ER00";
    protected static final String CMD_ER01 = "ER01";
    protected static final String CMD_ER02 = "ER02";
    protected static final String CMD_ER03 = "ER03";

    public ClientThread(Socket socket) {
        this.clientSocket = socket;
        this.isConnected = false;
        this.receivedPong = false;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;

            sendMessageToClient(CMD_INFO, "Welcome to server sucks ass");

            while ((inputLine = in.readLine()) != null) {
                String[] command = parseCommand(inputLine);

                switch (command[0]) {
                    case CMD_CONN -> {
                        username = command[1];
                        isConnected = true;
                        ChatServer.addClient(this);
                        sendMessageToClient(CMD_OK, username);
                        //TODO: print sth on the screen
                        ChatServer.stats();
                    }
                    case CMD_BCST -> {
                        String message = command[1];
                        for (ClientThread client: ChatServer.getClients()) {
                            if (client != this) {
                                client.sendMessageToClient(CMD_BCST, username + " " + message);
                                //TODO: print sth on the screen
                            }
                        }
                        sendMessageToClient(CMD_OK, CMD_BCST + " " + message);
                        //TODO: print sth on the screen
                    }
                    case CMD_PONG -> {
                        receivedPong = true;
                    }
                    case CMD_QUIT -> {
                        sendMessageToClient(CMD_OK, "Goodbye");
                        //TODO: print sth on the screen
                        ChatServer.stats();

                        stopConnection();
                    }
                    default -> sendMessageToClient(CMD_ER00, "Unknown command");
                }


//                if (inputLine.equals(".")) {
//                    sendMessageToClient(CMD_OK, "Goodbye");
//                    break;
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToClient(String header, String message) {
        this.out.println(header + ' ' + message);
        this.out.flush();
    }

    private String[] parseCommand(String input) {
        return input.split(" ", 2);
    }

    private void processMessage(String message) {

    }

    private void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void heartbeat() {
        System.out.printf("~~ %s Heartbeat initiated\n", username);

//        TODO: convert this to java
//        setTimeout(function () {
//            client.receivedPong = false
//            sendToClient(client, 'PING')
//            setTimeout(function () {
//                if (client.receivedPong) {
//                    console.log(`~~ [${client.username}] Heartbeat expired - SUCCESS`)
//                    heartbeat(client)
//                } else {
//                    console.log(`~~ [${client.username}] Heartbeat expired - FAILED`)
//                    sendToClient(client, 'DCSN')
//                    client.destroy()
//                }
//            }, 3 * 1000)
//        }, 10 * 1000)
    }

//    TODO: convert this to java
//    private boolean validUsernameFormat(String username) {
//        String pattern = "^[a-zA-Z0-9_]{3,14}$";
//        return username.match(pattern);
//    }
}
