package nl.saxion.itech.server;

import nl.saxion.itech.shared.security.RSA;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class IntegrationPacketBreakup {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    private final static int max_delta_allowed_ms = 100;
    public static final RSA RSA = new RSA();

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationPacketBreakup.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        s = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        s.close();
    }

    @Test
    @DisplayName("RQ-B202 - flushingMultipleTimesIsAllowed")
    void flushingMultipleTimesIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.print("CONN m");
        out.flush();
        out.print("yname " + RSA.getPublicKeyAsString() + "\r\nBC");
        out.flush();
        out.print("ST a\r\n");
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        // TODO: reflect in documentation updated protocol
        assertEquals("OK CONN myname "  + RSA.getPublicKeyAsString(), serverResponse);
        serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK BCST a", serverResponse);
        // logout to make sure tests succeed without restarting the server
        // Since a state of connected users is kept in memory
        out.println("QUIT");
        out.flush();
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}