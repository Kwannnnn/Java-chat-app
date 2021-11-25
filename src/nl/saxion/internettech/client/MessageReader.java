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
                String[] parsedLine = parseResponse(line);

                switch (parsedLine[0]) {
                    case CMD_INFO -> {
                        super.showWelcomeMessage();
                        super.askUsernameMessage();
                    }
                    case CMD_OK -> {
                        switch (parsedLine[1]) {
                            case CMD_BCST -> super.showSuccessfulBroadcastMessage(parsedLine[2]);
                            default -> {
                                super.showSuccessfulLoginMessage();
                                super.promptMenuMessage();
                                client.setCurrentUser(parsedLine[1]);
                            }
                        }
                    }
                    case CMD_PING -> sendPong();
                    case CMD_ER02 -> {
                        super.showErrorMessage(parsedLine[1]);
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

    private String[] parseResponse(String response) {
        return response.split(" ", 3);
    }

    private void sendPong() {
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
