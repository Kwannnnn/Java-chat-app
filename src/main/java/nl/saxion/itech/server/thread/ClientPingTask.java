package nl.saxion.itech.server.thread;

import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.util.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.TimerTask;

import static nl.saxion.itech.shared.ProtocolConstants.*;

public class ClientPingTask extends TimerTask {
    private static final String PING_MESSAGE = CMD_PING;
    private final Client client;
    private final PrintWriter out;
    private Logger logger;

    public ClientPingTask(Client client) {
        this.client = client;
        this.out = new PrintWriter(client.getOutputStream());
    }

    @Override
    public void run() {
        this.logger = Logger.getInstance();
        try {
            log("~~ [" + client + "] Heartbeat initiated");
            Thread.sleep(PING_INITIAL_DELAY_MS);
            client.setReceivedPong(false);
            pingClient();

            Thread.sleep(PING_TIME_MS);

            if (this.client.isReceivedPong()) {
                log("~~ [" + client + "] Heartbeat expired - SUCCESS");
            } else {
                log("~~ [" + client + "] Heartbeat expired - FAILED");
                disconnectClient();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void pingClient() {
        this.out.println(PING_MESSAGE);
        this.out.flush();
        log("<< [" + client + "] " + PING_MESSAGE);
    }

    private synchronized void disconnectClient() {
        try {
            this.out.println(CMD_DSCN + " " + CMD_TIMEOUT);
            this.client.getOutputStream().close();
            this.client.getInputStream().close();
            this.cancel();
        } catch (IOException e) {
            // Client socket already closed
        }
    }

    private void log(String text) {
        if (logger.isInitiated()) {
            this.logger.logMessage(text);
        }
    }
}
