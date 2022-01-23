package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.ProtocolInterpreter;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

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
            var fileInputStream = new FileInputStream(ChatClient.class.getResource(file.getName()).getFile());

            OutputStream outputStream = socket.getOutputStream();

            var socketPrintWriter = new PrintWriter(outputStream, true);
            var socketDataOutputStream = new DataOutputStream(outputStream);

            socketPrintWriter.println("UPLOAD " + fileID);
            socketPrintWriter.flush();

            fileInputStream.transferTo(socketDataOutputStream);
            socketDataOutputStream.close();

            ProtocolInterpreter.showFinishedFileUpload(fileID);
        } catch (IOException e) {
            //say something
            e.printStackTrace();
        } finally {
            this.client.removeFileToSend(file.getId());
        }
    }
}
