package nl.saxion.itech.server.threads;

import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.model.protocol.BaseMessage;
import static nl.saxion.itech.server.model.protocol.ProtocolConstants.*;

import java.time.Duration;
import java.time.Instant;

public class GroupPingThread extends Thread {
    private final Group group;
    private final ServiceManager manager;

    public GroupPingThread(Group group, ServiceManager manager) {
        this.group = group;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Thread.sleep(120 * 1000);
                Instant now = Instant.now();
                for (var entry : this.group.getLastMessageTimeStamp()) {
                    Duration difference = Duration.between(entry.getValue(), now);
                    if (difference.toMillis() > 120 * 1000) {
                        this.manager.dispatchMessage(new BaseMessage(
                                CMD_OK + " " + CMD_GRP + " " + CMD_DCSN,
                                this.group.getName(),
                                this.group.getClient(entry.getKey())
                        ));
                        this.group.removeClient(entry.getKey());
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
