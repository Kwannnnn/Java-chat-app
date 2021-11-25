package nl.saxion.internettech.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MessageSender extends ProtocolInterpreter implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private ChatClient client;

    public MessageSender(Socket socket, ChatClient client) {
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
            String user = this.client.getCurrentUser();
            if (user == null) {
                sendMessageToServer(CMD_CONN, input);
            } else {
                if (input.equals("?")) {
                    super.showMenu();
                } else if (input.equalsIgnoreCase("B")) {
                    super.enterMessageMessage();
                    header = CMD_BCST;
                    message = scanner.nextLine();
                    sendMessageToServer(header, message);
                }
            }
        } while (!input.equalsIgnoreCase("Q") && !socket.isClosed());

//        do {
//            input = scanner.nextLine();
//            this.writer.println(input);
//            this.writer.flush();
//        } while (!input.equalsIgnoreCase("Q"));
    }

    private void sendMessageToServer(String header, String message) {
        this.writer.println(header + ' ' + message);
        this.writer.flush();
    }


}
