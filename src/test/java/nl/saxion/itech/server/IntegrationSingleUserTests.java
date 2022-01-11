package nl.saxion.itech.server;

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
    private static final String VALID_USERNAME = "myname";
    private static final String INVALID_USERNAME = "*a*";
    private static final String VALID_GROUP_NAME = "cats";
    private static final String INVALID_GROUP_NAME = "*g*";


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
    @DisplayName("RQ-S100 - receiveInfoMessage")
    void receiveInfoMessage() {
        // given
        // when
        String firstLine = receiveLineWithTimeout(in); // INFO Welcome to the server
        // then
        assertTrue(firstLine.matches(CMD_INFO + " " + INFO_BODY));
    }

    @Test
    @DisplayName("RQ-U100 - loginSucceedsWithOK")
    void loginSucceedsWithOK() {
        receiveLineWithTimeout(in); // Receive info message
        // given
        var message = CMD_CONN + " " + VALID_USERNAME; // CONN myname
        sendMessage(message);
        // when
        String serverResponse = receiveLineWithTimeout(in); // OK CONN myname
        // then
        assertEquals(CMD_OK + " " + CMD_CONN + " " + VALID_USERNAME, serverResponse);
    }

    @Test
    @DisplayName("RQ-U100 - Bad Weather - loginEmptyNameWithER08")
    void loginEmptyNameWithER02() {
        receiveLineWithTimeout(in); // Receive info message
        // given
        sendMessage(CMD_CONN);
        // when
        String serverResponse = receiveLineWithTimeout(in);
        // then
        assertEquals(CMD_ER08 + " " + ER08_BODY, serverResponse);
    }

    @Test
    @DisplayName("RQ-U100 - Bad Weather - loginInvalidCharactersWithER02")
    void loginInvalidCharactersWithER02() {
        receiveLineWithTimeout(in); // Receive info message
        // given
        sendMessage(CMD_CONN + " " + INVALID_USERNAME); // CONN *a*
        // when
        String serverResponse = receiveLineWithTimeout(in); // ER02 Username has an invalid format (only characters, numbers and underscores are allowed)
        // then
        assertEquals(CMD_ER02 + " " + ER02_BODY, serverResponse);
    }

    @Test
    @DisplayName("RQ-S100 - pingShouldBeReceivedOnCorrectTime")
    void pingShouldBeReceivedOnCorrectTime(TestReporter testReporter) {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + VALID_USERNAME); // CONN myname
        receiveLineWithTimeout(in); // OK CONN myname

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
    @DisplayName("RQ-U202 - GRP NEW Message")
    void GRP_NEW() {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + VALID_USERNAME); // C: CONN myname
        String connServerResponse = receiveLineWithTimeout(in); // S: OK CONN myname
        assumeTrue(connServerResponse.startsWith(CMD_OK));

        // given
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME); // GRP NEW cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // OK GRP NEW cats
        // then
        assertEquals(CMD_OK + " " + CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U202 - Bad Weather - GRP NEW - grpNewWithoutBeingConnectedRespondsE03")
    void GRP_NEW_Bad_Weather_ER03() {
        receiveLineWithTimeout(in); // Receive info message

        // given
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME); // GRP NEW cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // ER03 Please log in first
        // then
        assertEquals(CMD_ER03 + " " + ER03_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U202 - Bad Weather - GRP NEW - grpNewWithInvalidGroupNameRespondsE05")
    void GRP_NEW_Bad_Weather_ER05() {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + VALID_USERNAME); // C: CONN myname
        String connServerResponse = receiveLineWithTimeout(in); // S: OK CONN myname
        assumeTrue(connServerResponse.startsWith(CMD_OK));

        // given
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + INVALID_GROUP_NAME); // GRP NEW cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // ER05 Group name has an invalid format (only characters, numbers and underscores are allowed)
        // then
        assertEquals(CMD_ER05 + " " + ER05_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U202 - Bad Weather - GRP NEW - grpNewGroupNameAlreadyExistsE06")
    void GRP_NEW_Bad_Weather_ER06() {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + VALID_USERNAME); // C: CONN myname
        String connServerResponse = receiveLineWithTimeout(in); // S: OK CONN myname
        assumeTrue(connServerResponse.startsWith(CMD_OK));

        // Create a group
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME); // GRP NEW cats
        String grpNewServerResponse = receiveLineWithTimeout(in); // OK GRP NEW cats
        assumeTrue(grpNewServerResponse.startsWith(CMD_OK));

        // given
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME); // GRP NEW cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // ER06 A group with this name already exists
        // then
        assertEquals(CMD_ER06 + " " + ER06_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U202 - Bad Weather - GRP NEW - grpNewWithMissingArgumentsRespondsE08")
    void GRP_NEW_Bad_Weather_ER08() {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + VALID_USERNAME); // C: CONN myname
        String connServerResponse = receiveLineWithTimeout(in); // S: OK CONN myname
        assumeTrue(connServerResponse.startsWith(CMD_OK));

        // Create a group with missing name
        sendMessage(CMD_GRP + " " + CMD_NEW); // GRP NEW
        // when
        String allServerResponse = receiveLineWithTimeout(in); // ER08 Missing parameters
        // then
        assertEquals(CMD_ER08 + " " + ER08_BODY, allServerResponse);
    }

    @Test
    @DisplayName("RQ-U203 - GRP JOIN Message")
    void GRP_JOIN() {
        receiveLineWithTimeout(in); // Receive info message
        sendMessage(CMD_CONN + " " + VALID_USERNAME); // C: CONN myname
        String connServerResponse = receiveLineWithTimeout(in); // S: OK CONN myname
        assumeTrue(connServerResponse.startsWith(CMD_OK));

        // First create a group
        sendMessage(CMD_GRP + " " + CMD_NEW + " " + VALID_GROUP_NAME); // GRP NEW cats
        String grpNewServerResponse = receiveLineWithTimeout(this.in);
        assumeTrue(connServerResponse.startsWith(CMD_OK));

        // given
        sendMessage(CMD_GRP + " " + CMD_JOIN + " " + VALID_GROUP_NAME); // GRP JOIN cats
        // when
        String allServerResponse = receiveLineWithTimeout(in); // OK GRP JOIN cats
        // then
        assertEquals(CMD_OK + " " + CMD_GRP + " " + CMD_JOIN + " " + VALID_GROUP_NAME, allServerResponse);
    }

    private void sendMessage(String message) {
        this.out.println(message);
        this.out.flush();
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), () -> reader.readLine());
    }

}