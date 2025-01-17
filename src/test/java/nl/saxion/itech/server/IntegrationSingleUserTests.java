package nl.saxion.itech.server;

import nl.saxion.itech.shared.security.RSA;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static nl.saxion.itech.shared.ProtocolConstants.*;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IntegrationSingleUserTests {
    private static final Properties PROPS = new Properties();
    private static final String TEST_CONFIG_FILENAME = "testconfig.properties";
    private static final int MAX_DELTA_ALLOWED_MS = 100;
    private static final String VALID_USERNAME = "Carlo";
    private static final String VALID_PASSWORD = "Password1";
    private static final String INVALID_USERNAME = "*a*";
    private static final String VALID_GROUP_NAME_1 = "cats";
    private static final String VALID_GROUP_NAME_2 = "dogs";
    private static final String INVALID_GROUP_NAME = "*g*";
    public static final RSA RSA = new RSA();


    private static int ping_time_ms;
    private static int ping_time_ms_delta_allowed;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationSingleUserTests.class.getResourceAsStream(TEST_CONFIG_FILENAME);
        PROPS.load(in);

        assert in != null : "Could not find the configuration file " + TEST_CONFIG_FILENAME + " in the resources folder";
        in.close();

        ping_time_ms = Integer.parseInt(PROPS.getProperty("ping_time_ms", "10000"));
        ping_time_ms_delta_allowed = Integer.parseInt(PROPS.getProperty("ping_time_ms_delta_allowed", "100"));
    }

    @BeforeEach
    void setup() throws IOException {
        this.socket = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
    }

    @AfterEach
    void cleanUp() throws IOException {
        this.socket.close();
    }

    @Test
    @DisplayName("emptyMessageShouldReturnUnknownCommand")
    void emptyMessage() {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage("");
        String response = receiveLineWithTimeout(in);
        assertEquals(CMD_ER00 + " " + ER00_BODY, response);
    }

    @Test
    @DisplayName("RQ-S100 - receiveInfoMessage")
    void receiveInfoMessage() {
        String firstLine = receiveLineWithTimeout(in); // INFO Welcome to the server
        assertTrue(firstLine.matches(CMD_INFO + " " + INFO_BODY));
    }

    @Test
    @DisplayName("RQ-U100 - CONN message - goodWeather")
    void CONN_Good_Weather() {
        receiveLineWithTimeout(in); // Receive info message
        // given
        var message = CMD_CONN + " " + VALID_USERNAME + " " + RSA.getPublicKeyAsString(); // CONN myname publicKey
        sendMessage(message);
        // when
        String serverResponse = receiveLineWithTimeout(in); // OK CONN myname publicKey
        // then
        assertEquals(CMD_OK + " " + message, serverResponse);
    }

    @Test
    @DisplayName("RQ-U100 - CONN message - badWeatherLoginEmptyNameWithER08")
    void CONN_Bad_Weather_ER08() {
        receiveLineWithTimeout(in); // Receive info message
        // given
        sendMessage(CMD_CONN);
        // when
        String serverResponse = receiveLineWithTimeout(in);
        // then
        assertEquals(CMD_ER08 + " " + ER08_BODY, serverResponse);
    }

    @Test
    @DisplayName("RQ-U100 - CONN message - badWeatherInvalidCharactersWithER02")
    void CONN_Bad_Weather_ER02() {
        receiveLineWithTimeout(in); // Receive info message
        // given
        sendMessage(CMD_CONN + " " + INVALID_USERNAME + " " + RSA.getPublicKeyAsString()); // CONN *a* publicKey
        // when
        String serverResponse = receiveLineWithTimeout(in); // ER02 Username has an invalid format (only characters, numbers and underscores are allowed)
        // then
        assertEquals(CMD_ER02 + " " + ER02_BODY, serverResponse);
    }

    @Test
    @DisplayName("RQ-S100 - pingShouldBeReceivedOnCorrectTime")
    void pingShouldBeReceivedOnCorrectTime(TestReporter testReporter) {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + VALID_USERNAME + " " + RSA.getPublicKeyAsString()); // CONN myname publicKey
        receiveLineWithTimeout(in); // OK CONN myname publicKey

        //Make sure the test does not hang when no response is received by using assertTimeoutPreemptively
        assertTimeoutPreemptively(ofMillis(ping_time_ms + ping_time_ms_delta_allowed), () -> {
            Instant start = Instant.now();
            String ping = in.readLine();
            Instant finish = Instant.now();

            // Make sure the correct response is received
            assertEquals(CMD_PING, ping);

            // Also make sure the response is not received too early
            long timeElapsed = Duration.between(start, finish).toMillis();
            testReporter.publishEntry("timeElapsed", "" + timeElapsed);
            assertTrue(timeElapsed > ping_time_ms - ping_time_ms_delta_allowed);
        });
    }

    @Test
    @DisplayName("RQ-S100 - PONG message")
    void PONG() {
        connectUser();

        //Make sure the test does not hang when no response is received by using assertTimeoutPreemptively
        String ping = receivePingWithTimeout(in);

        // Make sure the correct response is received
        assumeTrue(ping.equals(CMD_PING));

        //send PONG
        sendMessage(CMD_PONG);

        assertTimeoutPreemptively(ofMillis(3000), () -> {
            sendMessage(CMD_BCST + " test message");
            String response = receiveLineWithTimeout(in);
            assertFalse(response.startsWith("ER"));
        });
    }

    @Test
    @DisplayName("RQ-U211 - AUTH message - goodWeather")
    void AUTH_Good_Weather() {
        connectUser();

        sendMessage(CMD_AUTH + " " + VALID_PASSWORD);
        String response = receiveLineWithTimeout(in);

        assertEquals(CMD_OK + " " + CMD_AUTH, response);
    }

    @Test
    @DisplayName("RQ-U211 - AUTH message - badWeatherUserNotConnectedShouldReturnER03")
    void AUTH_Bad_Weather_ER03() {
        receiveLineWithTimeout(in); // Receive info message

        sendMessage(CMD_AUTH + " " + VALID_PASSWORD);
        String response = receiveLineWithTimeout(in);

        assertEquals(CMD_ER03 + " " + ER03_BODY, response);
    }

    @Test
    @DisplayName("RQ-U211 - AUTH message - badWeatherMissingParameterShouldReturnER08")
    void AUTH_Bad_Weather_ER08() {
        connectUser();

        // send username without password
        sendMessage(CMD_AUTH);
        String response = receiveLineWithTimeout(in);

        assertEquals(CMD_ER08 + " " + ER08_BODY, response);
    }

    @Test
    @DisplayName("RQ-U211 - AUTH message - badWeatherInvalidCredentialsShouldReturnER11")
    void AUTH_Bad_Weather_ER11() {
        connectUser();

        // send username without password
        sendMessage(CMD_AUTH + " " + "wrong password");
        String response = receiveLineWithTimeout(in);

        assertEquals(CMD_ER11 + " " + ER11_BODY, response);
    }

    @Test
    @DisplayName("RQ-U211 - AUTH message - badWeatherUserNotAuthenticatedShouldReturnER11")
    void AUTH_Bad_Weather_ER14() {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + "user" + " " + RSA.getPublicKeyAsString()); // C: CONN user publicKey
        String connServerResponse = receiveLineWithTimeout(in); // S: OK CONN user publicKey
        assumeTrue(connServerResponse.startsWith(CMD_OK));

        sendMessage(CMD_AUTH + " " + "password");
        String response = receiveLineWithTimeout(in);

        assertEquals(CMD_ER11 + " " + ER11_BODY, response);
    }

    @Test
    @DisplayName("RQ-U202 - GRP NEW Message - goodWeather")
    void GRP_NEW() {
        connectUser();

        // given
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME_1); // GRP NEW cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // OK GRP NEW cats
        // then
        assertEquals(CMD_OK + " " + CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME_1, allServerResponse);

        disconnectFromGroup1();
    }

    @Test
    @DisplayName("RQ-U202 - GRP - badWeatherGrpWithoutAnyParametersShouldReturnE08")
    void GRP_Bad_Weather_ER08() {
        connectUser();

        sendMessage(CMD_GRP);
        String response = receiveLineWithTimeout(in);
        assertEquals(CMD_ER08 + " " + ER08_BODY, response);
    }

    @Test
    @DisplayName("RQ-U202 - GRP - badWeatherGrpUnknownCommandShouldReturnE00")
    void GRP_Bad_Weather_ER00() {
        connectUser();

        sendMessage(CMD_GRP + " " + "UNKNOWN");
        String response = receiveLineWithTimeout(in);
        assertEquals(CMD_ER00 + " " + ER00_BODY, response);
    }

    @Test
    @DisplayName("RQ-U202 - GRP NEW - badWeatherUserNotConnectedShouldReturnE03")
    void GRP_NEW_Bad_Weather_ER03() {
        receiveLineWithTimeout(in); // Receive info message

        // given
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME_1); // GRP NEW cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // ER03 Please log in first
        // then
        assertEquals(CMD_ER03 + " " + ER03_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U202 - GRP NEW - badWeatherInvalidGroupNameShouldReturnE05")
    void GRP_NEW_Bad_Weather_ER05() {
        connectUser();

        // given
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + INVALID_GROUP_NAME); // GRP NEW cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // ER05 Group name has an invalid format (only characters, numbers and underscores are allowed)
        // then
        assertEquals(CMD_ER05 + " " + ER05_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U202 - GRP NEW - badWeatherGroupNameAlreadyExistsE06")
    void GRP_NEW_Bad_Weather_ER06() {
        connectUser();

        // Create a group
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME_1); // GRP NEW cats
        String grpNewServerResponse = receiveLineWithTimeout(in); // OK GRP NEW cats
        assumeTrue(grpNewServerResponse.startsWith(CMD_OK));

        // given
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME_1); // GRP NEW cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // ER06 A group with this name already exists
        // then
        assertEquals(CMD_ER06 + " " + ER06_BODY, allServerResponse);

        sendMessage(CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME_1);
        String grpDscnResponse = receiveLineWithTimeout(in); // OK GRP DSCN cats
        assumeTrue(grpDscnResponse.startsWith(CMD_OK));
    }

    @Test
    @DisplayName("RQ-U202 - GRP NEW - badWeatherMissingArgumentsShouldRespondE08")
    void GRP_NEW_Bad_Weather_ER08() {
        connectUser();

        // Create a group with missing name
        sendMessage(CMD_GRP + " " + CMD_NEW); // GRP NEW
        // when
        String allServerResponse = receiveLineWithTimeout(in); // ER08 Missing parameters
        // then
        assertEquals(CMD_ER08 + " " + ER08_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U204 - GRP ALL Message - goodWeather")
    void GRP_ALL() {
        connectUser();

        // First create a group
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME_1); // GRP NEW cats
        String grpNew1ServerResponse = receiveLineWithTimeout(this.in);
        assumeTrue(grpNew1ServerResponse.startsWith(CMD_OK));

        // Create another group
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME_2); // GRP NEW dogs
        String grpNew2ServerResponse = receiveLineWithTimeout(this.in);
        assumeTrue(grpNew2ServerResponse.startsWith(CMD_OK));

        // given
        sendMessage(CMD_GRP + " " + CMD_ALL); // GRP ALL
        // when
        String allServerResponse = receiveLineWithTimeout(in); // GRP ALL cats,dogs
        // then
        assertEquals(CMD_OK + " " + CMD_GRP + " " + CMD_ALL + " " + VALID_GROUP_NAME_1 + "," + VALID_GROUP_NAME_2, allServerResponse);

        sendMessage(CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME_1);
        String grpDscn1Response = receiveLineWithTimeout(in); // OK GRP DSCN cats
        assumeTrue(grpDscn1Response.startsWith(CMD_OK));

        sendMessage(CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME_2);
        String grpDscn2Response = receiveLineWithTimeout(in); // OK GRP DSCN dogs
        assumeTrue(grpDscn2Response.startsWith(CMD_OK));
    }

    @Test
    @DisplayName("RQ-U204 - GRP ALL Message - badWeatherUserNotConnectedShouldReturnER03")
    void GRP_ALL_Bad_Weather_ER03() {
        receiveLineWithTimeout(in); // Receive info message

        // given
        sendMessage(CMD_GRP + " " + CMD_ALL); // GRP ALL
        // when
        String allServerResponse = receiveLineWithTimeout(in);
        // then
        assertEquals(CMD_ER03 + " " + ER03_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U205 - GRP MSG Message - badWeatherNotLoggedInShouldReturnER03")
    void GRP_MSG_Bad_Weather_ER03() {
        receiveLineWithTimeout(in); //info message

        // sends GRP MSG message
        String message = "hello";
        sendMessage(CMD_GRP + " " + CMD_MSG + " " + VALID_GROUP_NAME_1 + " " + message);

        //response from server
        String response = receiveLineWithTimeout(in);
        assertEquals(CMD_ER03 + " " + ER03_BODY, response);
    }

    @Test
    @DisplayName("RQ-U205 - GRP MSG Message - badWeatherGroupDoesNotExistShouldReturnER07")
    void GRP_MSG_Bad_Weather_ER07() {
        connectUser();

        // sends GRP MSG message
        String message = "hello";
        sendMessage(CMD_GRP + " " + CMD_MSG + " " + "nonexistentGroup" + " " + message);

        //response from server
        String response = receiveLineWithTimeout(in);
        assertEquals(CMD_ER07 + " " + ER07_BODY, response);
    }

    @Test
    @DisplayName("RQ-U205 - GRP MSG Message - badWeatherMissingParametersShouldReturnER08")
    void GRP_MSG_Bad_Weather_ER08() {
        connectUser();

        // sends GRP MSG message
        String message = "hello";
        sendMessage(CMD_GRP + " " + CMD_MSG);

        //response from server
        String response = receiveLineWithTimeout(in);
        assertEquals(CMD_ER08 + " " + ER08_BODY, response);
    }

    @Test
    @DisplayName("RQ-U209 - GRP DSCN Message - Good Weather")
    void GRP_DSCN_Good_Weather() {
        connectUser();

        // First create a group
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME_1); // GRP NEW cats
        String grpNew1ServerResponse = receiveLineWithTimeout(this.in);
        assumeTrue(grpNew1ServerResponse.startsWith(CMD_OK));

        sendMessage(CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME_1);

        //response from server
        String response = receiveLineWithTimeout(in);
        assertEquals(CMD_OK + " " + CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME_1, response);
    }

    @Test
    @DisplayName("RQ-U102 - DSCN message - Good Weather")
    void DSCN_Good_Weather() {
        connectUser();

        sendMessage(CMD_DSCN);
        String response = receiveLineWithTimeout(in);
        assertEquals(CMD_OK + " " + CMD_DSCN, response);
    }

    private void sendMessage(String message) {
        this.out.println(message);
        this.out.flush();
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), () -> reader.readLine());
    }

    private String receivePingWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(ping_time_ms + ping_time_ms_delta_allowed), () -> reader.readLine());
    }

    private void connectUser() {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + VALID_USERNAME + " " + RSA.getPublicKeyAsString()); // C: CONN Carlo publicKey
        String connServerResponse = receiveLineWithTimeout(in); // S: OK CONN Carlo publicKey
        assumeTrue(connServerResponse.startsWith(CMD_OK));
    }

    private void disconnectFromGroup1() {
        sendMessage(CMD_GRP + " " + CMD_DSCN + " " + VALID_GROUP_NAME_1);
        String grpDscnResponse = receiveLineWithTimeout(in); // OK GRP DSCN cats
        assumeTrue(grpDscnResponse.startsWith(CMD_OK));
    }
}