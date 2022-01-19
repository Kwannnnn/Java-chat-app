package nl.saxion.itech.server;

import nl.saxion.itech.shared.security.AES;
import nl.saxion.itech.shared.security.RSA;
import nl.saxion.itech.shared.security.util.SecurityUtil;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static nl.saxion.itech.shared.ProtocolConstants.*;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IntegrationMultipleUserTests {
    private static final Properties PROPS = new Properties();
    private static final int MAX_DELTA_ALLOWED_MS = 100;
    private static final String TEST_CONFIG_FILENAME = "testconfig.properties";
    private static final String USERNAME_1 = "user1";
    private static final String USERNAME_2 = "user2";
    private static final String MESSAGE_1 = "message1";
    private static final String MESSAGE_2 = "message2";
    private static final String VALID_GROUP_NAME = "cats";
    public static final RSA RSA_USER_1 = new RSA();
    public static final RSA RSA_USER_2 = new RSA();
    public static final AES AES_USER_1 = new AES();
    public static final AES AES_USER_2 = new AES();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationMultipleUserTests.class.getResourceAsStream(TEST_CONFIG_FILENAME);
        PROPS.load(in);

        assert in != null : "Could not find the configuration file " + TEST_CONFIG_FILENAME + " in the resources folder";
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        var host = PROPS.getProperty("host");
        var port = Integer.parseInt(PROPS.getProperty("port"));

        this.socketUser1 = new Socket(host, port);
        this.inUser1 = new BufferedReader(new InputStreamReader(this.socketUser1.getInputStream()));
        this.outUser1 = new PrintWriter(this.socketUser1.getOutputStream(), true);

        this.socketUser2 = new Socket(host, port);
        this.inUser2 = new BufferedReader(new InputStreamReader(this.socketUser2.getInputStream()));
        this.outUser2 = new PrintWriter(this.socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        this.socketUser1.close();
        this.socketUser2.close();
    }

    @Test
    @DisplayName("RQ-U101 - BCST Message")
    void BCST() {
        receiveLineWithTimeout(this.inUser1); // Receive INFO message user1
        receiveLineWithTimeout(this.inUser2); // Receive INFO message user2

        // Connect user1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect user2
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + RSA_USER_2.getPublicKeyAsString()); // CONN user2 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user2 publicKey
        assumeTrue(resUser2.startsWith(CMD_OK));

        // Send BCST from user1
        var message1 = CMD_BCST + " " + MESSAGE_1;
        sendMessageUser1(message1); // BCST message one
        String serverResponseUser1 = receiveLineWithTimeout(inUser1); // OK BCST message1
        assertEquals(CMD_OK + " " + message1, serverResponseUser1);

        String messageToUser2 = receiveLineWithTimeout(inUser2); // BCST user1 message1
        assertEquals(CMD_BCST + " " + USERNAME_1 + " " + MESSAGE_1, messageToUser2);

        // Send BCST from user2
        var message2 = CMD_BCST + " " + MESSAGE_2;
        sendMessageUser2(message2); // BCST message one
        String serverResponseUser2 = receiveLineWithTimeout(inUser2); // OK BCST message2
        assertEquals(CMD_OK + " " + message2, serverResponseUser2);

        String messageToUser1 = receiveLineWithTimeout(inUser1); // BCST user2 message2
        assertEquals(CMD_BCST + " " + USERNAME_2 + " " + MESSAGE_2, messageToUser1);
    }

    @Test
    @DisplayName("RQ-U101 - Bad Weather - BCST - bcstWithoutBeingConnectedRespondsER03")
    void BCST_Bad_Weather_ER03() {
        receiveLineWithTimeout(this.inUser1); // Receive INFO message user1

        // BCST message1, while sender is not connected
        sendMessageUser1(CMD_BCST + " " + MESSAGE_1);
        String serverResponse = receiveLineWithTimeout(this.inUser1); // ER03 Please login first
        assertEquals(CMD_ER03 + " " + ER03_BODY, serverResponse);
    }

    @Test
    @DisplayName("RQ-U101 - Bad Weather - BCST - bcstWithMissingArgumentsRespondsER08")
    void BCST_Bad_Weather_ER08() {
        receiveLineWithTimeout(this.inUser1); // Receive INFO message user1
        receiveLineWithTimeout(this.inUser2); // Receive INFO message user2

        // Connect user1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // BCST - missing message
        sendMessageUser1(CMD_BCST);
        String serverResponse = receiveLineWithTimeout(this.inUser1); // ER08 Missing parameters
        assertEquals(CMD_ER08 + " " + ER08_BODY, serverResponse);
    }


    @Test
    @DisplayName("RQ-U200 - ALL Message")
    void ALL() {
        receiveLineWithTimeout(this.inUser1); // Receive INFO message user1
        receiveLineWithTimeout(this.inUser2); // Receive INFO message user2

        // Connect user1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect user2
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + RSA_USER_2.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // user1: ALL
        sendMessageUser1(CMD_ALL);
        String allServerResponse = receiveLineWithTimeout(this.inUser1); // OK ALL user1,user2
        assertEquals(CMD_OK + " " + CMD_ALL + " " + USERNAME_1 + "," + USERNAME_2, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U200 - Bad Weather - ALL - allWithoutBeingConnectedRespondsER03")
    void ALL_Bad_Weather_ER03() {
        receiveLineWithTimeout(this.inUser1); // Receive info message

        // ALL, while user is not connected
        sendMessageUser1(CMD_ALL);
        String allServerResponse = receiveLineWithTimeout(this.inUser1); // ER03 Please login first
        assertEquals(CMD_ER03 + " " + ER03_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U201 - MSG Message")
    void MSG() {
        receiveLineWithTimeout(this.inUser1); // Receive INFO message user1
        receiveLineWithTimeout(this.inUser2); // Receive INFO message user2

        // Connect user1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect user2
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user2 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user2 publicKey
        assumeTrue(resUser2.startsWith(CMD_OK));

        // Send MSG from user1 to user2
        var message1 = CMD_MSG + " " + USERNAME_2 + " " + MESSAGE_1;
        sendMessageUser1(message1); // MSG user2 message2
        String serverResponseUser1 = receiveLineWithTimeout(inUser1); // OK MSG user2 message1
        assertEquals(CMD_OK + " " + message1, serverResponseUser1);

        String messageToUser2 = receiveLineWithTimeout(inUser2); // MSG user1 message1
        assertEquals(CMD_MSG + " " + USERNAME_1 + " " + MESSAGE_1, messageToUser2);
    }

    @Test
    @DisplayName("RQ-U201 - Bad Weather - MSG - msgWithoutBeingConnectedRespondsER03")
    void MSG_Bad_Weather_ER03() {
        receiveLineWithTimeout(this.inUser1); // Receive INFO message user1

        // MSG user2 message1, while sender is not connected
        sendMessageUser1(CMD_MSG + " " + USERNAME_2 + " " + MESSAGE_1);
        String serverResponse = receiveLineWithTimeout(this.inUser1); // ER03 Please login first
        assertEquals(CMD_ER03 + " " + ER03_BODY, serverResponse);
    }

    @Test
    @DisplayName("RQ-U201 - Bad Weather - MSG - msgUnknownUserRespondsER04")
    void MSG_Bad_Weather_ER04() {
        receiveLineWithTimeout(this.inUser1); // Receive INFO message user1

        // Connect user1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // MSG user2 message1, while user2 is not connected
        sendMessageUser1(CMD_MSG + " " + USERNAME_2 + " " + MESSAGE_1);
        String serverResponse = receiveLineWithTimeout(this.inUser1); // ER04 The user you are trying to reach is not connected.
        assertEquals(CMD_ER04 + " " + ER04_BODY, serverResponse);
    }

    @Test
    @DisplayName("RQ-U201 - Bad Weather - MSG - msgWithMissingArgumentsRespondsER08")
    void MSG_Bad_Weather_ER08() {
        receiveLineWithTimeout(this.inUser1); // Receive INFO message user1
        receiveLineWithTimeout(this.inUser2); // Receive INFO message user2

        // Connect user1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + "  " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect user2
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + RSA_USER_2.getPublicKeyAsString()); // CONN user2 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user2 publicKey
        assumeTrue(resUser2.startsWith(CMD_OK));

        var expectedResult_ER08 = CMD_ER08 + " " + ER08_BODY; // ER08 Missing parameters

        // MSG - missing recipient and message
        sendMessageUser1(CMD_MSG);
        String serverResponse = receiveLineWithTimeout(this.inUser1); // ER08 Missing parameters
        assertEquals(expectedResult_ER08, serverResponse);

        // MSG user2 - missing message
        sendMessageUser1(CMD_MSG + " " + USERNAME_2);
        serverResponse = receiveLineWithTimeout(this.inUser1); // ER08 Missing parameters
        assertEquals(expectedResult_ER08, serverResponse);
    }

    @Test
    @DisplayName("RQ-S100 - Bad Weather - CONN - userAlreadyLoggedIn")
    void userAlreadyLoggedIn(){
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect using same username
        sendMessageUser2(CMD_CONN + " " + USERNAME_1 + "  " + RSA_USER_2.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // ER01 User already logged in
        assertEquals(CMD_ER01 + " " + ER01_BODY, resUser2);
    }

    @Test
    @DisplayName("RQ-U203 - GRP JOIN Message - shouldRespondWithOkGrpJoin")
    void GRP_JOIN() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect user2
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + RSA_USER_2.getPublicKeyAsString()); // CONN user2 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user2 publicKey
        assumeTrue(resUser2.startsWith(CMD_OK));

        // First create a group
        sendMessageUser1(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME); // GRP NEW cats
        String grpNewResponse = receiveLineWithTimeout(this.inUser1); // OK GRP NEW cats
        assumeTrue(grpNewResponse.startsWith(CMD_OK));

        // user2 joins the group
        sendMessageUser2(CMD_GRP + " " + CMD_JOIN + " " + VALID_GROUP_NAME); // GRP JOIN cats
        String user2Response = receiveLineWithTimeout(this.inUser2); // OK GRP JOIN cats
        assertEquals(CMD_OK + " " + CMD_GRP + " " + CMD_JOIN + " " + VALID_GROUP_NAME, user2Response);

        String grpJoinUser1Notification = receiveLineWithTimeout(this.inUser1); // GRP JOIN cats user2
        assertEquals(CMD_GRP + " " + CMD_JOIN + " " + VALID_GROUP_NAME + " " + USERNAME_2, grpJoinUser1Notification);

        // Cleanup
        sendMessageUser1(CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME); // GRP DSCN cats
        sendMessageUser2(CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME); // GRP DSCN cats
    }

    @Test
    @DisplayName("RQ-U203 - Bad Weather - GRP JOIN - grpJoinWithoutBeingConnectedRespondsER03")
    void GRP_JOIN_Bad_Weather_ER03() {
        receiveLineWithTimeout(inUser1); // Receive INFO message

        // given
        sendMessageUser1(CMD_GRP + " " + CMD_JOIN + " " + VALID_GROUP_NAME); // GRP JOIN cats
        // when
        String grpJoinServerResponse = receiveLineWithTimeout(this.inUser1); // ER03 Please log in first
        // then
        assertEquals(CMD_ER03 + " " + ER03_BODY, grpJoinServerResponse);
    }

    @Test
    @DisplayName("RQ-U203 - Bad Weather - GRP JOIN - grpJoinNonExistingGroupRespondsER07")
    void GRP_JOIN_Bad_Weather_ER07() {
        receiveLineWithTimeout(inUser1); // Receive INFO message

        // Connect user 1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Try to join group cats, which does not exist
        sendMessageUser1(CMD_GRP + " " + CMD_JOIN + " " + VALID_GROUP_NAME); // GRP JOIN cats
        // when
        String grpJoinResponse = receiveLineWithTimeout(this.inUser1); // ER07 A group with this name does not exist
        // then
        assertEquals(CMD_ER07 + " " + ER07_BODY, grpJoinResponse);
    }

    @Test
    @DisplayName("RQ-U203 - Bad Weather - GRP JOIN - grpJoinWithMissingArgumentsRespondsER08")
    void GRP_JOIN_Bad_Weather_ER08() {
        receiveLineWithTimeout(inUser1); // Receive INFO message

        // Connect user 1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // GRP JOIN - missing group name
        sendMessageUser1(CMD_GRP + " " + CMD_JOIN); // GRP JOIN
        String grpJoinResponse = receiveLineWithTimeout(this.inUser1); // ER08 Missing parameters
        assertEquals(CMD_ER08 + " " + ER08_BODY, grpJoinResponse);
    }

    @Test
    @DisplayName("RQ-U203 - Bad Weather - GRP JOIN - grpJoinWhileAlreadyPartOfGroupRespondsER09")
    void GRP_JOIN_Bad_Weather_ER09() {
        receiveLineWithTimeout(inUser1); // Receive INFO message

        // Connect user 1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Create and (automatically) join the group
        var grpNewMessage = CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME;
        sendMessageUser1(grpNewMessage); // GRP NEW cats
        String grpNewResponse = receiveLineWithTimeout(this.inUser1); // OK GRP NEW cats
        assertEquals(CMD_OK + " " + grpNewMessage, grpNewResponse);

        // GRP JOIN cats, while already a member of the group
        sendMessageUser1(CMD_GRP + " " + CMD_JOIN + " " + VALID_GROUP_NAME); // GRP JOIN
        String grpJoinResponse = receiveLineWithTimeout(this.inUser1); // ER09 You are already in this group
        assertEquals(CMD_ER09 + " " + ER09_BODY, grpJoinResponse);

        // Cleanup
        sendMessageUser1(CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME); // GRP DSCN cats
    }

    @Test
    @DisplayName("RQ-U400 - PUBK message - goodWeather")
    void PUBK_Good_Weather() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect user2
        var user2_publicKey = RSA_USER_2.getPublicKeyAsString();
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + user2_publicKey); // CONN user2 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user2 publicKey
        assumeTrue(resUser2.startsWith(CMD_OK));

        sendMessageUser1(CMD_PUBK + " " + USERNAME_2); // PUBK user2
        String response = receiveLineWithTimeout(this.inUser1);

        assertEquals(CMD_OK + " " + CMD_PUBK + " " + USERNAME_2 + " " + user2_publicKey, response);
    }

    @Test
    @DisplayName("RQ-U400 - PUBK message - Bad Weather - grpJoinWithoutBeingConnectedRespondsER03")
    void PUBK_Bad_Weather_ER03() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user2
        var user2_publicKey = RSA_USER_2.getPublicKeyAsString();
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + user2_publicKey); // CONN user2 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user2 publicKey
        assumeTrue(resUser2.startsWith(CMD_OK));

        sendMessageUser1(CMD_PUBK + " " + USERNAME_2); // PUBK user2
        String response = receiveLineWithTimeout(this.inUser1);

        assertEquals(CMD_ER03 + " " + ER03_BODY, response);
    }

    @Test
    @DisplayName("RQ-U400 - PUBK message - Bad Weather - pubkUnknownUserRespondsER04")
    void PUBK_Bad_Weather_ER04() {
        receiveLineWithTimeout(inUser1); //info message

        // Connect user1
        var user1_publicKey = RSA_USER_1.getPublicKeyAsString();
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + user1_publicKey); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        sendMessageUser1(CMD_PUBK + " " + USERNAME_2); // PUBK user2
        String response = receiveLineWithTimeout(this.inUser1);

        assertEquals(CMD_ER04 + " " + ER04_BODY, response);
    }

    @Test
    @DisplayName("RQ-U400 - PUBK message - Bad Weather - pubkWithoutUsernameParameterRespondsER08")
    void PUBK_Bad_Weather_ER08() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + RSA_USER_1.getPublicKeyAsString()); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect user2
        var user2_publicKey = RSA_USER_2.getPublicKeyAsString();
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + user2_publicKey); // CONN user2 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user2 publicKey
        assumeTrue(resUser2.startsWith(CMD_OK));

        sendMessageUser1(CMD_PUBK); // PUBK
        String response = receiveLineWithTimeout(this.inUser1);

        assertEquals(CMD_ER08 + " " + ER08_BODY, response);
    }

    @Test
    @DisplayName("RQ-U400 - SESSION message - goodWeather")
    void SESSION_Good_Weather() {
        receiveLineWithTimeout(inUser1); //info message
        receiveLineWithTimeout(inUser2); //info message

        // Connect user 1
        var user1_publicKey = RSA_USER_1.getPublicKeyAsString();
        var user1_privateKey = RSA_USER_1.getPrivateKey();
        var user1_sessionKeyString = AES_USER_1.getPrivateKeyAsString();
        sendMessageUser1(CMD_CONN + " " + USERNAME_1 + " " + user1_publicKey); // CONN user1 publicKey
        String resUser1 = receiveLineWithTimeout(this.inUser1); // OK CONN user1 publicKey
        assumeTrue(resUser1.startsWith(CMD_OK));

        // Connect user2
        var user2_publicKey = RSA_USER_2.getPublicKeyAsString();
        sendMessageUser2(CMD_CONN + " " + USERNAME_2 + " " + user2_publicKey); // CONN user2 publicKey
        String resUser2 = receiveLineWithTimeout(this.inUser2); // OK CONN user2 publicKey
        assumeTrue(resUser2.startsWith(CMD_OK));

        sendMessageUser1(CMD_PUBK + " " + USERNAME_2); // PUBK user2
        String response = receiveLineWithTimeout(this.inUser1);
        assumeTrue(response.equals(CMD_OK + " " + CMD_PUBK + " " + USERNAME_2 + " " + user2_publicKey));

        var encryptedSessionKey = SecurityUtil.encrypt(user1_sessionKeyString, user1_privateKey, "RSA");
        sendMessageUser1(CMD_SESSION + " " + USERNAME_2 + " " + encryptedSessionKey); // SESSION user2 Base64EncryptedSessionKey
        String responseSessionUser1 = receiveLineWithTimeout(this.inUser1);
        String user2message = receiveLineWithTimeout(this.inUser2);
        assumeTrue(user2message.equals(CMD_SESSION + " " + USERNAME_1 + " " + encryptedSessionKey));
        assertEquals(CMD_OK + " " + CMD_SESSION + " " + USERNAME_2 + " " + encryptedSessionKey, responseSessionUser1);
    }

    private void sendMessageUser1(String message) {
        this.outUser1.println(message);
        this.outUser1.flush();
    }

    private void sendMessageUser2(String message) {
        this.outUser2.println(message);
        this.outUser2.flush();
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), () -> reader.readLine());
    }

}