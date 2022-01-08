package nl.saxion.itech.server.service;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.model.File;

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
            // TODO: perhaps use DataInputStream instead containing this information
            // now it just simulates a "handshake" between the sender and receiver
            var dataInputStream = new BufferedReader(new InputStreamReader(in));

            var controlMessage = dataInputStream.readLine();
            var payload = new StringTokenizer(controlMessage);

            var mode = payload.nextToken();
            var transferID = payload.nextToken();
            var file = getFile(transferID); // Can throw Runtime Exception TODO: create more specific checked exception

            System.out.println(controlMessage);

            // Check if the client wants to receive the file or send the file
            switch (mode) {
                case "UPLOAD" -> updateFileUploader(file, in);
                case "DOWNLOAD" -> updateFileDownloader(file, out);
                default -> throw new Exception(); // TODO: handle error
            }

            // Wait until both parties have connected
            while(file.getSenderInputStream() == null
                    || file.getRecipientOutputStream() == null) {
                synchronized (this) {
                    wait();
                }
            }

            handleTransfer(file);
            // TODO: send checksum
            
        } catch (NoSuchElementException e) {
            // Missing parameters
        } catch (RuntimeException e) {
            // No such file
        } catch (IOException | InterruptedException e) {
            // Proceed to finally clause
        } catch (Exception e) {
            // Unknown command
        } finally {

        }
    }

    private void handleTransfer(File file) throws IOException {
        // TODO: use DataInputStream instead
        var in = new DataInputStream(file.getSenderInputStream());
        var out = new DataOutputStream(file.getRecipientOutputStream());

        // To simulate sending bytes, for now sending lines of text.
        // TODO: perhaps the code should look like the commented code below
//        String line;
//        while ((line = in.readLine()) != null) {
//            out.println(line);
//            out.flush();
//        }

        var fileSize = file.getFileSize();
        int readBytes = 0;
        byte[] chunk = new byte[16 * 1024];
        while (fileSize > 0 && (readBytes = in.read(chunk, 0, Math.min(chunk.length, fileSize))) != -1) {
            out.write(chunk);
            out.flush();
            fileSize -= readBytes;
        }

        // TODO: send checksum

        in.close();
        out.close();
    }

    private File getFile(String fileId) {
        var optional = this.data.getFile(fileId);
        System.out.println(fileId);

        if (optional.isEmpty()) {
            // TODO: handle error message
            throw new RuntimeException();
        }

        return optional.get();
    }

    private synchronized void updateFileUploader(File file, InputStream in) {
        file.setSenderInputStream(in);
        notify();
    }

    private synchronized void updateFileDownloader(File file, OutputStream out) {
        file.setRecipientOutputStream(out);
        notify();
    }
}
