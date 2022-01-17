package nl.saxion.itech.client;

import java.io.IOException;

public class ChatApp {

    public static void main(String[] args) {
        try {
            var client = new ChatClient();
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
