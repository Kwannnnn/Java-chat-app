package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.ProtocolInterpreter;

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
            ProtocolInterpreter.showFileNotFound(this.fileID);
            ProtocolInterpreter.showFileTransferProcessClosed();
            return;
        }
        var file = fileOptional.get();

        try {
            var fileStream = new FileInputStream(ChatClient.class.getResource(file.getName()).getFile());

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
                fileOut.write(chunk, 0, readBytes);
                fileOut.flush();
                size -= readBytes;
            }

            ProtocolInterpreter.showFinishedFileUpload(fileID);
        } catch (IOException e) {
            //say something
            e.printStackTrace();
        } finally {
            this.client.removeFileToSend(file.getId());
        }
    }
}
