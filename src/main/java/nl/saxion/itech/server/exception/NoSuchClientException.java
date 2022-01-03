package nl.saxion.itech.server.exception;

import java.io.IOException;

public class NoSuchClientException extends IOException {
    public NoSuchClientException(String message) {
        super(message);
    }
}
