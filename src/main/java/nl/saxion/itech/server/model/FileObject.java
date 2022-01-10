package nl.saxion.itech.server.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FileObject {
    private final String id;
    private final String filename;
    private final int fileSize;
    private final String checksum;
    private boolean isSent;
    private final Client sender;
    private final Client recipient;
    private InputStream senderInputStream;
    private OutputStream recipientOutputStream;

    public FileObject(String filename, Client sender, Client recipient, int fileSize, String checksum) {
        this.id = UUID.randomUUID().toString();
        this.filename = filename;
        this.sender = sender;
        this.recipient = recipient;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.isSent = false;
    }

    public void setSenderInputStream(InputStream senderInputStream) {
        this.senderInputStream = senderInputStream;
    }

    public void setRecipientOutputStream(OutputStream recipientOutputStream) {
        this.recipientOutputStream = recipientOutputStream;
    }

    public InputStream getSenderInputStream() {
        return senderInputStream;
    }

    public OutputStream getRecipientOutputStream() {
        return recipientOutputStream;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getId() {
        return id;
    }

    public Client getSender() {
        return sender;
    }

    public Client getRecipient() {
        return recipient;
    }

    public String getFilename() {
        return filename;
    }

    public String getChecksum() {
        return checksum;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

}
