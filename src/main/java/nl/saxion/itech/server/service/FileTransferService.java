package nl.saxion.itech.server.service;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.model.FileObject;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class FileTransferService implements Service {
    private final DataObject data;

    public FileTransferService(DataObject data) {
        this.data = data;
    }

    @Override
    public void serve(InputStream in, OutputStream out) {
        try {
            var dataIn = new DataInputStream(in);
            var dataOut = new DataOutputStream(out);

            var controlMessage = dataIn.readUTF();
            System.out.println(controlMessage);
            var payload = new StringTokenizer(controlMessage);

            var mode = payload.nextToken();
            var transferID = payload.nextToken();
            var file = getFile(transferID); // Can throw Runtime Exception

            // Check if the client wants to receive the file or send the file
            switch (mode) {
                case "UPLOAD" -> updateFileUploader(file, dataIn);
                case "DOWNLOAD" -> updateFileDownloader(file, dataOut);
                default -> throw new Exception();
            }

            // Wait until both parties have connected
            while(file.getSenderInputStream() == null
                    || file.getRecipientOutputStream() == null) {
                synchronized (this) {
                    wait();
                }
            }

            handleTransfer(file);
        } catch (NoSuchElementException e) {
            // Missing parameters
        } catch (RuntimeException e) {
            // No such file
        } catch (IOException | InterruptedException e) {
            // Proceed to finally clause
        } catch (Exception e) {
            // Unknown command
        }
    }

    private void handleTransfer(FileObject fileObject) throws IOException {
        var in = fileObject.getSenderInputStream();
        var out = fileObject.getRecipientOutputStream();

        in.transferTo(out);
    }

    private FileObject getFile(String fileId) {
        var optional = this.data.getFile(fileId);

        if (optional.isEmpty()) {
            throw new RuntimeException();
        }

        return optional.get();
    }

    private synchronized void updateFileUploader(FileObject fileObject, InputStream in) {
        fileObject.setSenderInputStream(in);
        notify();
    }

    private synchronized void updateFileDownloader(FileObject fileObject, OutputStream out) {
        fileObject.setRecipientOutputStream(out);
        notify();
    }
}
