package nl.saxion.itech.server.service;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.model.File;

import java.io.*;

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

            var mode = dataInputStream.readLine();
            var fileID = Long.parseLong(dataInputStream.readLine());
            var file = getFile(fileID);

            // Check if the client wants to receive the file or send the file
            switch (mode) {
                case "0" -> updateFileUploader(file, in);
                case "1" -> updateFileDownloader(file, out);
                default -> throw new RuntimeException(); // TODO: handle error
            }

            // Wait until both parties have connected
            while(file.getSenderInputStream() == null
            || file.getRecipientOutputStream() == null) {
                synchronized (this) {
                    wait();
                }
            }

            handleTransfer(file);
        } catch (IOException | InterruptedException e) {
            // Proceed to finally clause
        } finally {

        }
    }

    private void handleTransfer(File file) throws IOException {
        // TODO: use DataInputStream instead
        var in = new BufferedReader(new InputStreamReader(file.getSenderInputStream()));
        var out = new PrintWriter(file.getRecipientOutputStream());

        // To simulate sending bytes, for now sending lines of text.
        // TODO: perhaps the code should look like the commented code below
        String line;
        while ((line = in.readLine()) != null) {
            out.println(line);
            out.flush();
        }

//        var fileSize = file.getFileSize();
//        int bytes = 0;
//        byte[] chunk = new byte[16 * 1024];
//        while (fileSize > 0 && (in.read(chunk, 0, Math.min(chunk.length, fileSize))) != -1) {
//            in.transferTo(out);
//        }
    }

    private File getFile(long fileId) {
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
