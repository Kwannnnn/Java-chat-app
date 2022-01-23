package nl.saxion.itech.client.model;

public class FileObject {
    private final String id;
    private final String name;
    private final int fileSize;
    private final String checksum;

    public FileObject(String id, String name, int fileSize, String checksum) {
        this.id = id;
        this.name = name;
        this.fileSize = fileSize;
        this.checksum = checksum;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getChecksum() {
        return checksum;
    }
}
