package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;

import java.io.*;
import java.net.Socket;

public class FileUploadThread extends Thread {
    private final ChatClient client;
    private final String fileID;
    private final Socket socket;

    public FileUploadThread(ChatClient client, String fileID, Socket socket) {
        this.client = client;
        this.fileID = fileID;
        this.socket = socket;
    }

    @Override
    public void run() {
        var fileOptional = this.client.getFileToSend(this.fileID);
        if (fileOptional.isEmpty()) {
            System.out.println("Reached");
            return;
        }
        var file = fileOptional.get();

        try {
            var fileStream = ChatClient.class.getResourceAsStream(file.getName());
            OutputStream outputStream = socket.getOutputStream();

            var out = new PrintWriter(outputStream, true);
            var fileOut = new DataOutputStream(outputStream);

            out.println("UPLOAD " + fileID);
            out.flush();

            var size = file.getFileSize();
            int readBytes = 0;
            byte[] chunk = new byte[16 * 1024];
            while ((size > 0
                    && (readBytes = fileStream.read(chunk, 0, Math.min(chunk.length, size))) != -1)) {
                System.out.println(new String(chunk));
                fileOut.write(chunk);
                fileOut.flush();
                size -= readBytes;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.client.removeFileToSend(file);
        }
        super.run();
    }
}
