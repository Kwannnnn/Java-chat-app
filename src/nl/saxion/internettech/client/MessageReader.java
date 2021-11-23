package nl.saxion.internettech.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                String[] splittedString = line.split(" ", 2);
                String header = splittedString[0];
                String payload = splittedString[1];
                switch (header) {
                    case "INFO" -> {
                        super.showWelcomeMessage();
                        super.askUsernameMessage();
                    }
                    case "OK" -> {
                        if (payload.split(" ").length > 1) {
                            System.out.println(payload);
                        } else {
                            System.out.println("You have been successfully logged in!");
                            client.setCurrentUser(payload);
                            super.promptMenuMessage();
                        }
                    }
                    case "ER02" -> {
                        super.showInvalidUsernameMessage();
                        super.askUsernameMessage();
                    }
                }

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

    public void stopConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
