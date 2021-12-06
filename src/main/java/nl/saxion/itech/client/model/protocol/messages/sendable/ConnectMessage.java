package nl.saxion.itech.client.model.protocol.messages.sendable;

import nl.saxion.itech.client.model.protocol.visitors.SendableMessageVisitor;
import nl.saxion.itech.client.model.InvalidUsernameException;

public class ConnectMessage implements SendableMessage {
    private static final String HEADER = "CONN";
    private String message;

    public ConnectMessage(String message) throws InvalidUsernameException {
        if (isValidUsername(message)) {
            this.message = message;
        } else {
            throw new InvalidUsernameException("Username has an invalid format (only characters, numbers and underscores are allowed)");
        }
    }

    /**
     * Checks whether a username conforms to a certain pattern. Usernames must be between 3 and 14 characters, and can
     * only contain letters, numbers, and underscores.
     * @param username the username to be checked
     * @return true if the username matches the described patter, otherwise false.
     */
    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]{3,14}$");
    }

    @Override
    public void accept(SendableMessageVisitor messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + message;
    }
}
