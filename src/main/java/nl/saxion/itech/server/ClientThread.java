package nl.saxion.itech.server;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.InvalidUsernameException;
import nl.saxion.itech.server.model.protocol.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ClientThread extends Thread {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Client client;
    private ClientHandler clientHandler;
    private MessageVisitor messageVisitor;

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
        try {
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.clientHandler = ClientHandler.getInstance();
            this.messageVisitor = new MessageHandlerVisitor(this.clientSocket, this.out);
        }  catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        this.isConnected = false;
        this.receivedPong = false;
    }

    public void run() {
        try {
            String inputLine;

            messageVisitor.visit(new InfoMessage("Welcome to server 1"));

            while ((inputLine = in.readLine()) != null) {
                String[] command = parseCommand(inputLine);
                String header = command[0];
                String message = command[1];

                switch (header) {
                    case CMD_CONN -> handleConnectMessage(message);
                    case CMD_BCST -> handleBroadcastMessage(message);
                    case CMD_PONG -> receivedPong = true;
                    case CMD_QUIT -> handleQuitMessage();
                    default -> handleUnknownCommand();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnectMessage(String message) {
        try {
            messageVisitor.visit(new ConnectMessage(message));
//            heartbeat();
            //TODO: print sth on the screen
//            Logger.stats();
        } catch (InvalidUsernameException e) {
            // Send error message
            // TODO: make error message enumeration
            messageVisitor.visit(new ErrorMessage(e.getCode(), e.getMessage()));
        }
    }

    private void handleBroadcastMessage(String message) {
        messageVisitor.visit(new BroadcastMessage(message));
        //TODO: print sth on the screen
    }

    private void handleQuitMessage() {
        messageVisitor.visit(new OkMessage("Goodbye"));
        //TODO: print sth on the screen
//        Logger.stats();

        stopConnection();
    }

    private void handleUnknownCommand() {
        messageVisitor.visit(new ErrorMessage("ER00", "Unknown command"));
    }

    private void sendPing() {
        this.out.println(CMD_PING);
        this.out.flush();
    }

    private String[] parseCommand(String input) {
        return input.split(" ", 2);
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
}
