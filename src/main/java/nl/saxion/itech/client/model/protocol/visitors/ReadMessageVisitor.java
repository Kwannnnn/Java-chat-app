package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.ChatClient;
import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkBroadcastMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkConnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupAllMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupDisconnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupJoinMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupMessageMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.*;

public class ReadMessageVisitor implements MessageVisitor {
    private ChatClient client;

    public static final String ANSI_YELLOW = "\u001B[33;3m";
    public static final String ANSI_RESET = "\u001B[0m";

    public ReadMessageVisitor(ChatClient client) {
        this.client = client;
    }

    @Override
    public void visit(InfoMessage message) {
        System.out.println(message.getMessage());
    }

    @Override
    public void visit(OkConnectMessage message) {
        this.client.setCurrentUser(message.getBody());
        System.out.println("You have been successfully logged in.");
        System.out.println(ANSI_YELLOW + "Type '?' to show menu." + ANSI_RESET);
    }

    @Override
    public void visit(OkBroadcastMessage message) {
        System.out.println("Broadcast message successfully sent.");
    }

    @Override
    public void visit(ErrorMessage message) {
        System.out.println(message.getBody());
    }

    @Override
    public void visit(OkGroupJoinMessage message) {
        System.out.println("You have successfully joined group: " + message.getBody());
    }

    @Override
    public void visit(OkGroupAllMessage message) {
        String[] groups = parseMessageBody(message.getBody(), ",");

        System.out.println("====List of groups====");
        for (String group : groups) {
            System.out.println(group);
        }
        System.out.println("======================");

    }

    @Override
    public void visit(OkGroupMessageMessage message) {
        //the body of this message contains 2 parts: group name and the message that was sent
        String[] messageParts = parseMessageBody(message.getBody(), " ", 2);
        String groupName = messageParts[0];
        String sentMessage = messageParts[1];

        // decide if we want to show the group name and message
        System.out.println("Group message successfully sent");
    }

    @Override
    public void visit(OkGroupDisconnectMessage message) {
        System.out.println("Successfully disconnected from group: " + message.getBody());
    }

    @Override
    public void visit(GroupJoinMessage message) {
        //the body of this message contains 2 parts: group name and the new group member
        String[] messageParts = parseMessageBody(message.getBody(), " ", 2);
        String groupName = messageParts[0];
        String newMember = messageParts[1];

        System.out.println("New member in group \"" + groupName + "\": " + newMember);
    }

    @Override
    public void visit(GroupMessageMessage message) {
        //the body of this message contains 2 parts: group name and the message that was sent
        String[] messageParts = parseMessageBody(message.getBody(), " ", 2);
        String groupName = messageParts[0];
        String receivedMessage = messageParts[1];

        System.out.println("New message in group \"" + groupName + "\": " + receivedMessage);
    }

    @Override
    public void visit(GroupDisconnectMessage message) {

    }

    @Override
    public void visit(GroupNewMessage message) {

    }

    @Override
    public void visit(QuitMessage message) {

    }

    private String[] parseMessageBody(String body, String delimiter) {
        return body.split(delimiter);
    }

    private String[] parseMessageBody(String body, String delimiter, int limit) {
        return body.split(delimiter, limit);
    }


}
