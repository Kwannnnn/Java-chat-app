package nl.saxion.internettech.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MessageWriter implements Runnable {
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

//        while (!message.equals("QUIT")) {
//
//            if (message.equals("HELP")) {
//                ChatClient.showMenu();
//                continue;
//            }
//
//            this.writer.println(message);
//            this.writer.flush();
//        }

        do {
            message = scanner.nextLine();

            if (message.equals("HELP")) {
                ChatClient.showMenu();
                continue;
            }

            this.writer.println(message);
            this.writer.flush();
        } while (!message.equals("QUIT"));

        // Close the connection whenever the message is quit
//        try {
//            socket.close();
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }

    }


}
