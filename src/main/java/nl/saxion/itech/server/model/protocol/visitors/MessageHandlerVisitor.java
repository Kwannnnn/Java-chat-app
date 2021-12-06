package nl.saxion.itech.server.model.protocol.visitors;

import nl.saxion.itech.server.services.ClientHandler;
import nl.saxion.itech.server.model.Client;
import nl.saxion.itech.server.model.protocol.messages.*;

import java.io.PrintWriter;
import java.net.Socket;

public class MessageHandlerVisitor implements MessageVisitor {
    private ClientHandler clientHandler;
    private final Socket clientSocket;
    private final PrintWriter clientPrintWriter;

    public MessageHandlerVisitor(Socket clientSocket, PrintWriter clientPrintWriter) {
        this.clientSocket = clientSocket;
        this.clientPrintWriter = clientPrintWriter;
        this.clientHandler = ClientHandler.getInstance();
    }

    @Override
    public void visit(ConnectMessage message) {
        this.clientHandler.addClient(new Client(message.getMessage(), this.clientSocket));
        new OkMessage(message.toString()).accept(this);
    }

    @Override
    public void visit(OkMessage message) {
        this.clientPrintWriter.println(message.toString());
    }

    @Override
    public void visit(InfoMessage message) {
        this.clientPrintWriter.println(message.toString());
    }

    @Override
    public void visit(ErrorMessage message) {
        this.clientPrintWriter.println(message.toString());
    }

    @Override
    public void visit(BroadcastMessage message) {
        for (var client : this.clientHandler.getClients()) {
            if (client.getSocket() != this.clientSocket) {
                //TODO: send message to client
                //TODO: print sth on the screen

            }
        }
        this.visit(new OkMessage(message.toString()));
    }
}
