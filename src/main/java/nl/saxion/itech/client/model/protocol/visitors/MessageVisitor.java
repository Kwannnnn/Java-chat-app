package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkBroadcastMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.OkConnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupAllMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupDisconnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupJoinMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupMessageMessage;
import nl.saxion.itech.client.model.protocol.messages.sendable.*;

public interface MessageVisitor {
    void visit(InfoMessage message);
    void visit(OkConnectMessage message);
    void visit(OkBroadcastMessage message);
    void visit(ErrorMessage message);
    void visit(OkGroupJoinMessage message);
    void visit(OkGroupAllMessage message);
    void visit(OkGroupMessageMessage message);
    void visit(OkGroupDisconnectMessage message);
    void visit(GroupDisconnectMessage message);
    void visit(GroupJoinMessage message);
    void visit(GroupMessageMessage message);
    void visit(GroupNewMessage message);
    void visit(QuitMessage message);
}
