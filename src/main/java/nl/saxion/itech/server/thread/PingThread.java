package nl.saxion.itech.server.thread;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.util.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;

import static nl.saxion.itech.shared.ProtocolConstants.CLIENT_TIMEOUT_DURATION;
import static nl.saxion.itech.shared.ProtocolConstants.CMD_PING;

public class PingThread extends Thread {
    private final DataObject data;

    public PingThread(DataObject data) {
        this.data = data;
    }

    @Override
    public void run() {
        var logger = Logger.getInstance();
        var timeoutLimit = CLIENT_TIMEOUT_DURATION * 1000; // in milliseconds

        try {
            while (!isInterrupted()) {
                Thread.sleep(timeoutLimit);
                for (var client : this.data.getAllClients()) {
                    if (client.getLastPong() == null) {
                        client.updateLastPong();
                        logger.logMessage("~~ [" + client + "] Heartbeat initiated");
                        pingClient(client);
                        continue;
                    }

                    var difference = Duration.between(client.getLastPong(), Instant.now());
                    if (difference.toMillis() > timeoutLimit + 100) {
                        logger.logMessage("~~ [" + client + "] Heartbeat expired - FAILED");
                        disconnectClient(client);
                    } else {
                        logger.logMessage("~~ [" + client + "] Heartbeat expired - SUCCESS");
                        pingClient(client);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void pingClient(Client client) {
//        assert !client.getSocket().isClosed() : "Client's socket is closed";

        var out = new PrintWriter(client.getOutputStream());
        out.println(CMD_PING);
        out.flush();
    }

    private synchronized void disconnectClient(Client client) {
        try {
            client.getOutputStream().close();
            client.getInputStream().close();
        } catch (IOException e) {
            // Client socket already closed
        }
    }
}
