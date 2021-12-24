package nl.saxion.itech.server.threads;

import nl.saxion.itech.server.model.protocol.BaseMessage;

import java.time.Duration;
import java.time.Instant;

import static nl.saxion.itech.shared.ProtocolConstants.*;

public class PingThread extends Thread {
    private final ServiceManager manager;

    public PingThread(ServiceManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Thread.sleep(CLIENT_TIMEOUT_DURATION * 500);
                Instant now = Instant.now();
                for (var entry : this.manager.getTimestampsOfClients()) {
                    Duration difference = Duration.between(entry.getValue(), now);
                    if (difference.toMillis() > CLIENT_TIMEOUT_DURATION * 1000) {
                        String username = entry.getKey();
                        this.manager.dispatchMessage(new BaseMessage(
                                CMD_DSCN + " " + CMD_PONG,
                                "timeout",
                                this.manager.getClient(username)
                        ));
                        this.manager.removeClient(username);
                    } else {
                        this.manager.dispatchMessage(new BaseMessage(
                                CMD_PING,
                                "",
                                this.manager.getClient(entry.getKey())
                        ));
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
