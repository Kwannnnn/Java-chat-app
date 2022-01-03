package nl.saxion.itech.server.service;

import nl.saxion.itech.server.data.DataObject;
import nl.saxion.itech.server.model.Client;

public class FileTransferService implements Service {
    private final DataObject data;

    public FileTransferService(DataObject data) {
        this.data = data;
    }

    @Override
    public void serve(Client sender) {

    }
}
