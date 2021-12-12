package nl.saxion.itech.client.model.protocol.visitors;

import nl.saxion.itech.client.model.protocol.messages.receivable.InfoMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ReceivableGroupJoinMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.ReceivableGroupMessageMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.*;
import nl.saxion.itech.client.model.protocol.messages.receivable.ErrorMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupAllMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupDisconnectMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupJoinMessage;
import nl.saxion.itech.client.model.protocol.messages.receivable.okmessages.groupMessages.OkGroupMessageMessage;

public interface ReceivableMessageVisitor {
    void visit(InfoMessage message);
    void visit(OkConnectMessage message);
    void visit(OkBroadcastMessage message);
    void visit(ErrorMessage message);
    void visit(OkGroupJoinMessage message);
    void visit(OkGroupAllMessage message);
    void visit(OkGroupMessageMessage message);
    void visit(OkGroupDisconnectMessage message);
    void visit(ReceivableGroupJoinMessage receivableGroupJoinMessage);
    void visit(ReceivableGroupMessageMessage receivableGroupMessageMessage);
}
