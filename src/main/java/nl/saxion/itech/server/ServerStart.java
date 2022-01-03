package nl.saxion.itech.server;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.service.FileTransferService;
import nl.saxion.itech.server.service.MessageService;

import java.io.IOException;
import java.util.Properties;

/**
 * A main() method for running the server as a standalone program.
 */
public class ServerStart {
    private static final Properties props = new Properties();
    private int chatPort;
    private int fileTransferPort;

    public ServerStart() {
        try {
            props.load(this.getClass().getResourceAsStream("config.properties"));
            this.chatPort = Integer.parseInt(props.getProperty("chat_port"));
            this.fileTransferPort = Integer.parseInt(props.getProperty("ft_port"));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ServerStart().run();
    }

    private void run() {
        DataObject data = new DataObject();
        Server s = new Server(data, System.out);
        try {
            s.addService(new MessageService(data), this.chatPort);
            s.addService(new FileTransferService(data), this.fileTransferPort);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
