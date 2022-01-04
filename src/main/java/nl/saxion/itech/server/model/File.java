package nl.saxion.itech.server.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

public class File {
    private static final AtomicLong COUNT = new AtomicLong(0);
    private final Long id;
    private final String checksum;
    private final String filename;
    private final int fileSize;
    private InputStream senderInputStream;
    private OutputStream recipientOutputStream;

    public File(String filename, int fileSize, String checksum) {
        this.id = 1L;
        this.filename = filename;
        this.fileSize = fileSize;
        this.checksum = checksum;
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

    public Long getId() {
        return id;
    }
}
