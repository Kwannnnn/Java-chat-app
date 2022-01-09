package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.ProtocolInterpreter;

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
        var fileOptional = this.client.getFileToReceive(this.fileID);
        if (fileOptional.isEmpty()) {
            return;
        }
        var file = fileOptional.get();

        try {
            InputStream inputStream = this.socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            var dis = new DataInputStream(inputStream);
            var out = new PrintWriter(outputStream, true);
            var fileOutput = new BufferedOutputStream(new FileOutputStream(file.getName()));

            out.println("DOWNLOAD " + fileID);
            out.flush();

            var size = file.getFileSize();
            int readBytes = 0;
            byte[] chunk = new byte[16 * 1024];
            while (size > 0 && (readBytes = dis.read(chunk, 0, Math.min(chunk.length, size))) != -1) {
                fileOutput.write(chunk, 0, readBytes);
                fileOutput.flush();
                size -= readBytes;
            }

            // TODO: check checksum

            ProtocolInterpreter.showFileDownloadSuccess(fileID);

            socket.close();
            fileOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            this.client.removeFileToReceive(file);
        }
    }
}
