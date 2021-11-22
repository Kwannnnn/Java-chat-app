package nl.saxion.internettech.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MessageWriter extends ProtocolInterpreter implements Runnable {
    private Socket socket;
    private PrintWriter writer;

    public MessageWriter(Socket socket) {
        this.socket = socket;

        try {
            OutputStream outputStream = socket.getOutputStream();
            this.writer = new PrintWriter(outputStream);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String message;
        String input;
        String header;

        // TODO: It does not break from this loop
        do {
            System.out.println(ChatClient.isLoggedIn);
            message = scanner.nextLine();
            sendMessageToServer(CMD_CONN, message);
        }
        while(!ChatClient.isLoggedIn);

        do {
            System.out.println(ChatClient.isLoggedIn);
            input = scanner.nextLine();
            if (input.equals("?")) {
                super.showMenu();
            } else if (input.equals("B")) {
                header = CMD_BCST;
                message = scanner.nextLine();
                sendMessageToServer(header, message);
            }
        } while (!input.equals("Q"));
    }

    private void sendMessageToServer(String header, String message) {
        this.writer.println(header + ' ' + message);
        this.writer.flush();
    }


}
