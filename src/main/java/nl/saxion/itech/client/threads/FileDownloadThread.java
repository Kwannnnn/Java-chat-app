package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.ProtocolInterpreter;
import nl.saxion.itech.client.model.message.BaseMessage;
import nl.saxion.itech.client.util.FileChecksum;
import static nl.saxion.itech.shared.ProtocolConstants.*;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

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
        var fileObject = fileOptional.get();

        try {
            var inputStream = this.socket.getInputStream();
            var out = new DataOutputStream(this.socket.getOutputStream());
            var fileOutput = new FileOutputStream(fileObject.getName());

            out.writeUTF(CMD_DOWNLOAD + " " + fileID);
            out.flush();

            inputStream.transferTo(fileOutput);

            fileOutput.close();

            // compare checksum
            File downloadedFile = new File(fileObject.getName());
            String downloadedFileChecksum = FileChecksum.getFileChecksumMD5(downloadedFile);

            if (downloadedFileChecksum.equals(fileObject.getChecksum())) {
                sendFileTrSuccessMessage(fileID);
                ProtocolInterpreter.showFileDownloadSuccess(fileID);
            } else {
                sendFileTrFailMessage(fileID);
                ProtocolInterpreter.showFileDownloadFailure(fileID);
            }
        } catch (IOException e) {
            // Error occurred with one of the input streams
            // Proceed to finally clause
        } catch (NoSuchAlgorithmException e) {
            // Technically impossible if java does not change the security package
            throw new AssertionError(e);
        } finally {
            this.client.removeFileToReceive(fileObject.getId());
            closeSocket();
        }
    }

    private void sendFileTrSuccessMessage(String fileID) {
        this.client.addMessageToQueue(new BaseMessage(
                CMD_FILE + " " + CMD_COMPLETE,
                CMD_SUCCESS + " " + fileID));
    }

    private void sendFileTrFailMessage(String fileID) {
        this.client.addMessageToQueue(new BaseMessage(
                CMD_FILE + " " + CMD_COMPLETE,
                CMD_FAIL + " " + fileID));
    }

    private void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException e) {
            // Socket has already been closed
            // Do nothing further
        }
    }
}
