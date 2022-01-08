package nl.saxion.itech.client.newDesign;

public class File {
    private final String id;
    private final String name;
    private final int fileSize;

    public File(String id, String name, int fileSize) {
        this.id = id;
        this.name = name;
        this.fileSize = fileSize;
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
}
