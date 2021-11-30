package nl.saxion.itech.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
                        if (validUsernameFormat(username)) {
                            isConnected = true;
                            ChatServer.addClient(this);
                            sendMessageToClient(CMD_OK, username);
                            heartbeat();
                        } else {
                            sendMessageToClient(CMD_ER02, "Username has an invalid format (only characters, numbers and underscores are allowed)");
                        }

                        //TODO: print sth on the screen
                        ChatServer.stats();
                    }
                    case CMD_BCST -> {
                        String message = command[1];
                        for (ClientThread client : ChatServer.getClients()) {
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

    private void sendPing() {
        this.out.println(CMD_PING);
        this.out.flush();
    }

    private String[] parseCommand(String input) {
        return input.split(" ", 2);
    }

    private void processMessage(String message) {

    }

    private boolean validUsernameFormat(String username) {
        var pattern = "^[a-zA-Z0-9_]{3,14}$";
        return username.matches(pattern);
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

        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
            receivedPong = false;
            sendPing();

            CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> {
                if (receivedPong) {
                    System.out.printf("~~ %s Heartbeat expired - SUCCESS\n", username);
                    heartbeat();
                } else {
                    System.out.printf("~~ %s Heartbeat expired - FAILED\n", username);
                }
            });
        });
    }

//    TODO: convert this to java
//    private boolean validUsernameFormat(String username) {
//        String pattern = "^[a-zA-Z0-9_]{3,14}$";
//        return username.match(pattern);
//    }
}
