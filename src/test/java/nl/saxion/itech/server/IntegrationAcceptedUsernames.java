package nl.saxion.itech.server;

import nl.saxion.itech.shared.security.RSA;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationAcceptedUsernames {

    private static Properties props = new Properties();

    private Socket s;
    private BufferedReader in;
    private PrintWriter out;

    private final static int max_delta_allowed_ms = 100;
    public static final RSA RSA = new RSA();

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = IntegrationAcceptedUsernames.class.getResourceAsStream("testconfig.properties");
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
    @DisplayName("RQ-B202 - threeCharactersIsAllowed")
    void threeCharactersIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN mym " + RSA.getPublicKeyAsString());
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertEquals("OK CONN mym " + RSA.getPublicKeyAsString(), serverResponse);
    }

    @Test
    @DisplayName("RQ-B202 - twoCharactersIsNotAllowed")
    void twoCharactersIsNotAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN my " + RSA.getPublicKeyAsString());
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Too short username accepted: "+serverResponse);
    }

    @Test
    @DisplayName("RQ-B202 - fourteenCharactersIsAllowed")
    void fourteenCharactersIsAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN abcdefghijklmn "   + RSA.getPublicKeyAsString());
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        // TODO: reflect in documentation updated protocol
        assertEquals("OK CONN abcdefghijklmn " + RSA.getPublicKeyAsString(), serverResponse);
    }

    @Test
    @DisplayName("RQ-B202 - fifteenCharactersIsNotAllowed")
    void fifteenCharactersIsNotAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN abcdefghijklmop " + RSA.getPublicKeyAsString());
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Too long username accepted: "+serverResponse);
    }

    @Test
    @DisplayName("RQ-B202 - slashRIsNotAllowed")
    void slashRIsNotAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN a\rlmn " + RSA.getPublicKeyAsString());
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER08"), "Wrong character accepted");
    }

    @Test
    @DisplayName("RQ-B202 - bracketIsNotAllowed")
    void bracketIsNotAllowed() {
        receiveLineWithTimeout(in); //info message
        out.println("CONN a)lmn " + RSA.getPublicKeyAsString());
        out.flush();
        String serverResponse = receiveLineWithTimeout(in);
        assertTrue(serverResponse.startsWith("ER02"), "Wrong character accepted");
    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }

}