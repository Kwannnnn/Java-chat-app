package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.ProtocolInterpreter;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import static nl.saxion.itech.shared.ProtocolConstants.CMD_DOWNLOAD;
import static nl.saxion.itech.shared.ProtocolConstants.CMD_UPLOAD;

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
            var in = new BufferedInputStream(new FileInputStream(ChatClient.class.getResource(file.getName()).getFile()));
            var out = new DataOutputStream(this.socket.getOutputStream());

            out.writeUTF(CMD_UPLOAD + " " + fileID);
            out.flush();

            in.transferTo(out);
            in.close();

            ProtocolInterpreter.showFinishedFileUpload(fileID);
            this.socket.close();
        } catch (IOException e) {
            //say something
            e.printStackTrace();
        } finally {
            this.client.removeFileToSend(file.getId());
        }
    }
}
