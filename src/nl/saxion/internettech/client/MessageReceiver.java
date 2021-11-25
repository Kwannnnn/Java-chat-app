package nl.saxion.internettech.client;

import java.io.*;
import java.net.Socket;

public class MessageReceiver extends ProtocolInterpreter implements Runnable {
    private final Socket socket;
    private BufferedReader reader;
    private final ChatClient client;

    public MessageReceiver(Socket socket, ChatClient client) {
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
        try {
            while (socket.isConnected()) {
                String line = reader.readLine();
                if (line == null) break;

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
                    case CMD_BCST -> super.displayMessage(parsedLine[1], parsedLine[2]);
                    case CMD_PING -> sendPong();
                    case CMD_ER00, CMD_ER01, CMD_ER02, CMD_ER03 -> {
                        super.showErrorMessage(parsedLine[1]);
                        super.askUsernameMessage();
                    }
                }
            }

            stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
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
            reader.close();
            if (!socket.isClosed()) socket.close();
            super.connectionLost();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
