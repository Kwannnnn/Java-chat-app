package nl.saxion.itech.client.threads;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.ProtocolInterpreter;
import nl.saxion.itech.client.newDesign.BaseMessage;
import nl.saxion.itech.client.newDesign.FileChecksum;
import static nl.saxion.itech.shared.ProtocolConstants.*;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
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
            InputStream inputStream = this.socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            var dis = new DataInputStream(inputStream);
            var out = new PrintWriter(outputStream, true);
            var fileOutput = new BufferedOutputStream(new FileOutputStream(fileObject.getName()));

            out.println(CMD_DOWNLOAD + " " + fileID);
            out.flush();

            var size = fileObject.getFileSize();
            int readBytes = 0;
            byte[] chunk = new byte[16 * 1024];
            while (size > 0 && (readBytes = dis.read(chunk, 0, Math.min(chunk.length, size))) != -1) {
                fileOutput.write(chunk, 0, readBytes);
                fileOutput.flush();
                size -= readBytes;
            }

            // compare checksum
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            File downloadedFile = new File(fileObject.getName());
            String downloadedFileChecksum = FileChecksum.getFileChecksum(md5Digest, downloadedFile);

            if (downloadedFileChecksum.equals(fileObject.getChecksum())) {
                sendFileTrSuccessMessage(fileID);
                ProtocolInterpreter.showFileDownloadSuccess(fileID);
            } else {
                sendFileTrFailMessage(fileID);
                downloadedFile.delete();
                ProtocolInterpreter.showFileDownloadFailure(fileID);
            }

            socket.close();
            fileOutput.close();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            this.client.removeFileToReceive(fileObject.getId());
        }
    }

    private void sendFileTrSuccessMessage(String fileID) {
        this.client.addMessageToQueue(new BaseMessage(
                CMD_FILE + " " + CMD_TR,
                CMD_SUCCESS + " " + fileID));
    }

    private void sendFileTrFailMessage(String fileID) {
        this.client.addMessageToQueue(new BaseMessage(
                CMD_FILE + " " + CMD_TR,
                CMD_FAIL + " " + fileID));
    }
}
