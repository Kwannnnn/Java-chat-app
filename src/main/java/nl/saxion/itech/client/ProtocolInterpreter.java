package nl.saxion.itech.client;

public abstract class ProtocolInterpreter {
    protected static final int TERMINAL_SIZE = 80;

    protected static final String CMD_CONN = "CONN";
    protected static final String CMD_BCST = "BCST";
    protected static final String CMD_OK = "OK";
    protected static final String CMD_INFO = "INFO";
    protected static final String CMD_PING = "PING";
    protected static final String CMD_PONG = "PONG";
    protected static final String CMD_ER00 = "ER00";
    protected static final String CMD_ER01 = "ER01";
    protected static final String CMD_ER02 = "ER02";
    protected static final String CMD_ER03 = "ER03";

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32;51m";
    public static final String ANSI_YELLOW = "\u001B[33;3m";
    public static final String ANSI_MAGENTA = "\u001B[95m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_ITALIC = "\u001B[3m";
    public static final String ANSI_RESET = "\u001B[0m";


    protected void showMenu() {
        System.out.print(ANSI_MAGENTA +
                """
                B: \t Broadcast a message to every client on the server
                Q: \t Close connection with the server
                ?: \t Show this menu
                """ + ANSI_RESET);
    }

    protected void showWelcomeMessage(String message) {
        System.out.println();
        message = " ".repeat(TERMINAL_SIZE) + "\n" + message + "\n" + " ".repeat(TERMINAL_SIZE);
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
        System.out.println();
    }

    protected void askUsernameMessage() {
        System.out.print(bold(">> Please enter your username to log in: "));
    }

    protected void promptMenuMessage() {
        System.out.println(ANSI_YELLOW + "Type '?' to show menu." + ANSI_RESET);
    }

    protected void showErrorMessage(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }

    protected void showSuccessfulLoginMessage() {
        System.out.println(italic("You have been successfully logged in!"));
    }

    protected void showSuccessfulBroadcastMessage(String message) {
        System.out.println(italic("Successfully broadcast message: ") + message);
    }

    protected void enterMessageMessage() {
        System.out.print(bold(">> Please enter your message: "));
    }

    protected void displayMessage(String sender, String message) {
        System.out.println(bold("[" + sender + "]: " ) + message);
    }

    protected void connectionLost() {
        System.out.println(ANSI_RED + "\nYou have been disconnected from the server!" + ANSI_RESET);
    }

    private String italic(String string) {
        return ANSI_ITALIC + string + ANSI_RESET;
    }

    private String bold(String string) {
        return ANSI_BOLD + string + ANSI_RESET;
    }

    protected String centerText(String text) {
        String result = "";

        String emptySpaces = " ".repeat((TERMINAL_SIZE - text.length()) / 2);

        result = emptySpaces + text + emptySpaces;
        return result;
    }
}
