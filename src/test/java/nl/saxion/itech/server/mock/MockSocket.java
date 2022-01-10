package nl.saxion.itech.server.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MockSocket extends Socket {
    private String input = "";
    private List<Byte> receivedBytes = new ArrayList<>();

    public MockSocket() {

    }

    /**
     * @return an InputStream with a dummy request
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(input.getBytes());
    }

    public void setInput(String input) {
        this.input = input;
    }

    public OutputStream getOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                receivedBytes.add((byte) b);
            }
        };
    }

    public String getLatestResponse() {
        byte[] converted = toByteArray(receivedBytes);
        var fullStack = new String(converted, StandardCharsets.UTF_8).trim();

        var lastNewLineCharacterIndex = fullStack.lastIndexOf("\n");
        return lastNewLineCharacterIndex < 0
                ? fullStack
                : fullStack.substring(fullStack.lastIndexOf("\n")).trim();
    }

    private byte[] toByteArray(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        int index = 0;
        for (byte b : byteList) {
            byteArray[index++] = b;
        }
        return byteArray;
    }
}
