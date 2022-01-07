package nl.saxion.itech.server.thread;

import nl.saxion.itech.server.model.Group;
import nl.saxion.itech.server.util.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;

import static nl.saxion.itech.shared.ProtocolConstants.*;

public class GroupPingThread extends Thread {
    private final Group group;

    public GroupPingThread(Group group) {
        this.group = group;
    }

    @Override
    public void run() {
        var logger = Logger.getInstance();

        try {
            while (!isInterrupted()) {
                Thread.sleep(GROUP_PING_TIME_MS);
                for (var entry : this.group.getLastMessageTimeStamp()) {
                    var difference = Duration.between(entry.getValue(), Instant.now());
                    var username = entry.getKey();
                    if (difference.toMillis() > GROUP_PING_TIME_MS + PING_TIME_MS_DELTA_ALLOWED) {
                        var user = this.group.getClient(username);
                            var out = new PrintWriter(user.getOutputStream());
                            var message = CMD_GRP + " " + CMD_DSCN + " " + this.group.getName();
                            out.println(message);
                            out.flush();
                            logger.logMessage("~~ [" + username + "] Group Heartbeat expired - FAILED");
                            this.group.removeClient(entry.getKey());
                    } else {
                        logger.logMessage("~~ [" + username + "] Group Heartbeat expired - SUCCESS");
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
