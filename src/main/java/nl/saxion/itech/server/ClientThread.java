package nl.saxion.itech.server;

import nl.saxion.itech.server.model.protocol.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageVisitor messageVisitor;
    private MessageFactory messageFactory;

    public ClientThread(Socket socket) {
        this.clientSocket = socket;
        try {
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.messageVisitor = new MessageHandlerVisitor(this.clientSocket, this.out);
            this.messageFactory = new MessageFactory();
        }  catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run() {
        try {
            String inputLine;

            new InfoMessage("Welcome to server 1").accept(messageVisitor);

            while ((inputLine = in.readLine()) != null) {
                String[] command = parseCommand(inputLine);
                String header = command[0];
                String body =  command.length > 1 ? command[1] : "";

                var message = this.messageFactory.getMessage(header, body);
                message.accept(this.messageVisitor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void sendPing() {
//        this.out.println("PING");
//        this.out.flush();
//    }

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

//    private void heartbeat() {
//        System.out.printf("~~ %s Heartbeat initiated\n", username);
//
//        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
//            receivedPong = false;
//            sendPing();
//
//            CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> {
//                if (receivedPong) {
//                    System.out.printf("~~ %s Heartbeat expired - SUCCESS\n", username);
//                    heartbeat();
//                } else {
//                    System.out.printf("~~ %s Heartbeat expired - FAILED\n", username);
//                }
//            });
//        });
//    }
}
