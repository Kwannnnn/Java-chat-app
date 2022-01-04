package nl.saxion.itech.shared;

public final class ProtocolConstants {
    public static final String INFO_BODY = "Welcome to the server";

    public static final String CMD_INFO = "INFO";
    public static final String CMD_CONN = "CONN";
    public static final String CMD_BCST = "BCST";
    public static final String CMD_PING = "PING";
    public static final String CMD_PONG = "PONG";
    public static final String CMD_OK = "OK";
    public static final String CMD_DSCN = "DSCN";
    public static final String CMD_ALL = "ALL";
    public static final String CMD_MSG = "MSG";
    public static final String CMD_JOIN = "JOIN";
    public static final String CMD_GRP = "GRP";
    public static final String CMD_NEW = "NEW";
    public static final String CMD_FILE = "FILE";
    public static final String CMD_SEND = "SEND";
    public static final String CMD_ACCEPT = "ACCEPT";

    public static final String CMD_ER00 = "ER00";
    public static final String CMD_ER01 = "ER01";
    public static final String CMD_ER02 = "ER02";
    public static final String CMD_ER03 = "ER03";
    public static final String CMD_ER04 = "ER04";
    public static final String CMD_ER05 = "ER05";
    public static final String CMD_ER06 = "ER06";
    public static final String CMD_ER07 = "ER07";
    public static final String CMD_ER08 = "ER08";
    public static final String CMD_ER09 = "ER09";
    public static final String CMD_ER10 = "ER10";
    public static final String CMD_ER11 = "ER11";
    public static final String CMD_ER12 = "ER12";
    public static final String CMD_ER13 = "ER13";
    public static final String CMD_ER66 = "ER66";

    public static final String ER00_BODY = "Unknown command";
    public static final String ER01_BODY = "User already logged in";
    public static final String ER02_BODY = "Username has an invalid format (only characters, numbers and underscores are allowed)";
    public static final String ER03_BODY = "Please log in first";
    public static final String ER04_BODY = "The user you are trying to reach is not connected.";
    public static final String ER05_BODY = "Group name has an invalid format (only characters, numbers and underscores are allowed)";
    public static final String ER06_BODY = "A group with this name already exists";
    public static final String ER07_BODY = "A group with this name does not exist";
    public static final String ER08_BODY = "Missing parameters";
    public static final String ER09_BODY = "You are already in this group";
    public static final String ER10_BODY = "You are not part of this group";
    public static final String ER11_BODY = "Password does not match";
    public static final String ER12_BODY = "You have already been authenticated";
    public static final String ER13_BODY = "User with this username does not exist";
    public static final String ER66_BODY = "You are already logged in";
    public static final String DSCN_BODY = "Goodbye";

    public static final int GROUP_TIMEOUT_DURATION = 120;
    public static final int CLIENT_TIMEOUT_DURATION = 3;

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 14;

    public static boolean isValidGroupName(String groupName) {
        var pattern = String.format(
                "^[a-zA-Z0-9_]{%d,%d}$",
                MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH);
        return groupName.matches(pattern);
    }

    public static boolean isValidUsername(String username) {
        var pattern = "^[a-zA-Z0-9_]{3,14}$";
        return username.matches(pattern);
    }
}
