package nl.saxion.internettech.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageReader extends ProtocolInterpreter implements Runnable {
    private Socket socket;
    private BufferedReader reader;

    public MessageReader(Socket socket) {
        this.socket = socket;

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
                String header = line.split(" ")[0];
                switch (header) {
                    case "INFO" -> {
                        super.showWelcomeMessage();
                        super.askUsernameMessage();
                    }
                    case "OK" -> {
                        System.out.println("You have been successfully logged in!");
                        ChatClient.isLoggedIn = true;
                        super.promptMenuMessage();
                    }
                    case "ER02" -> {
                        System.err.println("Username in invalid format!");
                        super.askUsernameMessage();
                    }
                }

                if (line == null) {
                    stopConnection();
                    break;
                }

//                System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    // TODO: interface



    public void stopConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
