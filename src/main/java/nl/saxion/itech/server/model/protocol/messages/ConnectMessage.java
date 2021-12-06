package nl.saxion.itech.server.model.protocol.messages;

import nl.saxion.itech.server.model.exceptions.InvalidUsernameException;
import nl.saxion.itech.server.model.protocol.visitors.MessageVisitor;

public class ConnectMessage implements Message {
    private static final String HEADER = "CONN";
    private final String message;

    public ConnectMessage(String message) throws InvalidUsernameException {
        if (validUsernameFormat(message)) {
            this.message = message;
        } else {
            throw new InvalidUsernameException("Username has an invalid format (only characters, numbers and underscores are allowed)");
        }
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return HEADER + " " + message;
    }

    private boolean validUsernameFormat(String username) {
        var pattern = "^[a-zA-Z0-9_]{3,14}$";
        return username.matches(pattern);
    }
}
