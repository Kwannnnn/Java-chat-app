package nl.saxion.itech.server.model;

public class InvalidUsernameException extends Exception {
    private static final String CODE = "ER02";

    public InvalidUsernameException(String message) {
        super(message);
    }

    public String getCode() {
        return CODE;
    }
}
