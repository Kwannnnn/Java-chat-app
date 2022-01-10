package nl.saxion.itech.server.service;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.mock.MockSocket;
import static nl.saxion.itech.shared.ProtocolConstants.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageServiceUnitTest {
    private static final String VALID_USERNAME_1 = "username";
    private static final String VALID_USERNAME_2 = "username2";
    private static final String VALID_BCST_MSG = "Hello world!";

    private final MockSocket socket1 = new MockSocket();
    private final MockSocket socket2 = new MockSocket();
    private MessageService underTest;

    @BeforeEach
    void setUp() {
        this.underTest = new MessageService(new DataObject());
    }

    @Test
    @DisplayName("INFO message received")
    void receivesInfoMessage() {
        this.underTest.serve(this.socket1.getInputStream(), this.socket1.getOutputStream());
        var serverResponse = getServerResponseClient1();
        var expectedResponse = getMessage(CMD_INFO, INFO_BODY);
        assertEquals(expectedResponse, serverResponse);
    }

    @Test
    @DisplayName("Sending commands before CONN should respond with ER03")
    void sendCommandBeforeConn() {
        client1SendsRequest(CMD_ALL);
        var serverResponse = getServerResponseClient1();
        var expectedResponse = getMessage(CMD_ER03, ER03_BODY);
        assertEquals(expectedResponse, serverResponse);
    }

    @Test
    @DisplayName("CONN without params should respond with ER08")
    void connWithoutParams() {
        client1SendsRequest(CMD_CONN);
        var serverResponse = getServerResponseClient1();
        var expectedResponse = getMessage(CMD_ER08, ER08_BODY);
        assertEquals(expectedResponse, serverResponse);
    }

    @Test
    @DisplayName("CONN with taken username should respond with ER01")
    void connWithAlreadyConnectedUsername() {
        // First Client
        var client1Request = getMessage(CMD_CONN, VALID_USERNAME_1);
        client1SendsRequest(client1Request);
        var serverResponseClient1 = getServerResponseClient1();
        var expectedResponseClient1 = getMessage(CMD_OK, client1Request);
        assertEquals(expectedResponseClient1,
                serverResponseClient1,
                "First user failed to connect");

        // Second Client
        var client2Request = getMessage(CMD_CONN, VALID_USERNAME_1);
        client2SendsRequest(client2Request);
        var serverResponseClient2 = getServerResponseClient2();
        var expectedResponseClient2 = getMessage(CMD_ER01, ER01_BODY);
        assertEquals(expectedResponseClient2, serverResponseClient2);
    }

    @Test
    @DisplayName("Successful CONN should respond with OK CONN")
    void connWithAValidUsername() {
        var clientRequest = getMessage(CMD_CONN, VALID_USERNAME_1);
        client1SendsRequest(clientRequest);
        var serverResponse = getServerResponseClient1();
        var expectedResponse = getMessage(CMD_OK, clientRequest);
        assertEquals(expectedResponse, serverResponse);
    }

    @Test
    @DisplayName("Successful BCST should respond with OK BCST")
    void validBcstRequest() {
        // First login client
        var loginRequest = getMessage(CMD_CONN, VALID_USERNAME_1);
        // Send a broadcast message
        var clientRequest = getMessage(CMD_BCST, VALID_BCST_MSG);
        client1SendsRequest(loginRequest + "\n" + clientRequest);
        // Get Server response
        var serverResponse = getServerResponseClient1();

        // Check if response matches expected result
        var expectedResponse = getMessage(CMD_OK, clientRequest);
        assertEquals(expectedResponse, serverResponse);
    }

    @Test
    @DisplayName("BCST without message should respond with ER08")
    void bcstWithoutMessage() {
        String clientRequest = "";

        // First login client
        clientRequest += getMessage(CMD_CONN, VALID_USERNAME_1) + "\n";
        // Send a broadcast message
        clientRequest += CMD_BCST + "\n";
        client1SendsRequest(clientRequest);

        // Get Server response
        var serverResponse = getServerResponseClient1();

        // Check if response matches expected result
        var expectedResponse = getMessage(CMD_ER08, ER08_BODY);
        assertEquals(expectedResponse, serverResponse);
    }

    @Test
    @DisplayName("BCST is received by other clients")
    void bcstIsReceivedByOtherClients() {
        // First Client
        String client1Request = getMessage(CMD_CONN, VALID_USERNAME_1);
        client1SendsRequest(client1Request);
        assertEquals(CMD_OK + " " + client1Request,
                getServerResponseClient1(),
                "First user failed to connect");


        // Second client
        String client2Request = "";
        // First login second client
        client2Request += getMessage(CMD_CONN, VALID_USERNAME_2) + "\n";
        // Send a broadcast message
        var broadcastMessage = VALID_BCST_MSG;
        client2Request += getMessage(CMD_BCST, broadcastMessage) + "\n";
        client2SendsRequest(client2Request);
        // Second client expected response
        var expectedResponseClient2 = getMessage(CMD_OK, CMD_BCST + " " + broadcastMessage);
        assertEquals(expectedResponseClient2,
                getServerResponseClient1(),
                "Sending broadcast message from Client 2 failed.");

        // Get Server response
        var serverResponse = getServerResponseClient2();

        // Check if response matches expected result
        var expectedResponse = getMessage(CMD_BCST, VALID_USERNAME_2 + " " + broadcastMessage);
        assertEquals(expectedResponse, serverResponse);
    }

    private void client1SendsRequest(String request) {
        this.socket1.setInput(request);
        this.underTest.serve(this.socket1.getInputStream(), this.socket1.getOutputStream());
    }

    private void client2SendsRequest(String request) {
        this.socket2.setInput(request);
        this.underTest.serve(this.socket2.getInputStream(), this.socket2.getOutputStream());
    }

    private String getServerResponseClient1() {
        return this.socket1.getLatestResponse();
    }

    private String getServerResponseClient2() {
        return this.socket2.getLatestResponse();
    }

    private String getMessage(String header, String payload) {
        return header + " " + payload;
    }
}
