package nl.saxion.itech.server.model.protocol;

public final class ProtocolConstants {
    public static final String CMD_INFO = "INFO";
    public static final String CMD_CONN = "CONN";
    public static final String CMD_BCST = "BCST";
    public static final String CMD_PONG = "PONG";
    public static final String CMD_OK = "OK";
    public static final String CMD_QUIT = "QUIT";
    public static final String CMD_ALL = "ALL";
    public static final String CMD_MSG = "MSG";
    public static final String CMD_JOIN = "JOIN";
    public static final String CMD_GRP = "GRP";
    public static final String CMD_NEW = "NEW";

    public static final String CMD_ER00 = "ER00";
    public static final String CMD_ER01 = "ER01";
    public static final String CMD_ER02 = "ER02";
    public static final String CMD_ER03 = "ER03";
    public static final String CMD_ER04 = "ER04";
    public static final String CMD_ER08 = "ER08";
    public static final String CMD_ER66 = "ER66";

    public static final String ER00_BODY = "Unknown command";
    public static final String ER01_BODY = "User already logged in";
    public static final String ER02_BODY = "Username has an invalid format (only characters, numbers and underscores are allowed)";
    public static final String ER03_BODY = "Please log in first";
    public static final String ER04_BODY = "The user you are trying to reach is not connected.";
    public static final String ER08_BODY = "Missing parameters";
    public static final String ER66_BODY = "You are already logged in";


    private ProtocolConstants() {

    }
}
