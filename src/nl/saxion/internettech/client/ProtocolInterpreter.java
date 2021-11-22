package nl.saxion.internettech.client;

public abstract class ProtocolInterpreter {
    protected static final String CMD_CONN = "CONN";
    protected static final String CMD_BCST = "BCST";

    protected void showMenu() {
        System.out.print(
                """
                        L: \t\t Login to the server with a username
                        B: \t\t Broadcast a message to every client on the server
                        P: \t\t Reply to the server's PING request
                        Q: \t\t Close connection with the server
                        ?: \t\t Show this menu
                        
                        """);
    }

    protected void showWelcomeMessage() {
        System.out.println("Welcome to the server!");
    }

    protected void askUsernameMessage() {
        System.out.println("Please enter your username to log in: ");
    }

    protected void promptMenuMessage() {
        System.out.println("Type '?' to show menu.");
    }
}
