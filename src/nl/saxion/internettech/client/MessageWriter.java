package nl.saxion.internettech.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MessageWriter extends ProtocolInterpreter implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private ChatClient client;

    public MessageWriter(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

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

        do {
            input = scanner.nextLine();
            if (this.client.getCurrentUser() == null) {
                sendMessageToServer(CMD_CONN, input);
            } else {
                if (input.equals("?")) {
                    super.showMenu();
                } else if (input.equalsIgnoreCase("B")) {
                    header = CMD_BCST;
                    message = scanner.nextLine();
                    sendMessageToServer(header, message);
                }
            }
        } while (!input.equalsIgnoreCase("Q"));
    }

    private void sendMessageToServer(String header, String message) {
        this.writer.println(header + ' ' + message);
        this.writer.flush();
    }
}
