package nl.saxion.itech.client.model;

import nl.saxion.itech.client.model.protocol.*;

public class MessageFactory {

    public Message get(String header, String message) {
        return switch (header) {
            case "OK" -> new OkMessage(message);
            case "BCST" -> new BroadcastMessage(message);
            case "CONN" -> new ConnectMessage(message);
            case "INFO" -> new InfoMessage(message);
            case "QUIT" -> new QuitMessage();
            default -> new ErrorMessage();
        };
    }
}
