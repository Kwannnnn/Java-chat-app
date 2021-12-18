package nl.saxion.itech.server.threads;

import nl.saxion.itech.server.model.Client;

import java.io.IOException;
import java.io.PrintWriter;

public class PingThread extends Thread {
    private Client client;
    private final ServiceManager serviceManager;
    private PrintWriter out;

    public PingThread(Client client, ServiceManager serviceManager) {
        this.client = client;
        this.serviceManager = serviceManager;
    }

    @Override
    public void run() {
        try {
            try {
                this.out = new PrintWriter(this.client.getSocket().getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!isInterrupted()) {
                this.client.setHasPonged(false);
                Thread.sleep(10 * 1000);
//                this.out.println("PING");
//                Thread.sleep(3 * 1000);
//                if (!client.isHasPonged()) {
//                    this.dispatcher.removeClient(client);
//                    Thread.currentThread().interrupt();
//                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
