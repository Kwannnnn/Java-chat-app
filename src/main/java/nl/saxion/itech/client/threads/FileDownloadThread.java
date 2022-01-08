package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;

import java.io.*;
import java.net.Socket;

public class FileDownloadThread extends Thread {
    private final ChatClient client;
    private final String fileID;
    private final Socket socket;

    public FileDownloadThread(ChatClient client, String fileID, Socket socket) {
        this.client = client;
        this.fileID = fileID;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = this.socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            var in = new DataInputStream(inputStream);
            var out = new PrintWriter(outputStream, true);

            out.println("DOWNLOAD " + fileID);
            out.flush();

            byte[] chunk = new byte[16 * 1024];
            while (!socket.isClosed()) {
                in.read(chunk, 0, chunk.length);
                System.out.println(new String(chunk));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.run();
    }
}
