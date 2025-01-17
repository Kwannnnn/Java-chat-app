package nl.saxion.itech.server.model;

import java.io.*;
import java.util.UUID;

public class FileObject {
    private final String id;
    private final String filename;
    private boolean isSent;
    private long fileSize;
    private String checksum;
    private final Client sender;
    private final Client recipient;
    private InputStream senderInputStream;
    private OutputStream recipientOutputStream;

    public FileObject(String filename, long fileSize, String checksum, Client sender, Client recipient) {
        this.id = UUID.randomUUID().toString();
        this.filename = filename;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.sender = sender;
        this.recipient = recipient;
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

    public long getFileSize() {
        return fileSize;
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
