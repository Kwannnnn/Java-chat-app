package nl.saxion.internettech.client;

import java.io.*;
import java.net.Socket;

public class MessageReader extends ProtocolInterpreter implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private ChatClient client;

    public MessageReader(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream inputStream = this.socket.getInputStream();
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String line = reader.readLine();
                String[] splitString = line.split(" ", 2);
                String header = splitString[0];
                switch (header) {
                    case "INFO" -> {
                        super.showWelcomeMessage();
                        super.askUsernameMessage();
                    }
                    case "OK" -> {
                        String payload = splitString[1];
                        if (payload.split(" ").length > 1) {
                            System.out.println(payload);
                        } else {
                            System.out.println("You have been successfully logged in!");
                            client.setCurrentUser(payload);
                            super.promptMenuMessage();
                        }
                    }
                    case "PING" -> {
                        sendPong();
                    }
                    case "ER02" -> {
                        super.showInvalidUsernameMessage();
                        super.askUsernameMessage();
                    }
                }

//                System.out.println(line);

                if (line == null) {
                    stopConnection();
                    break;
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void sendPong() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println("PONG");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
