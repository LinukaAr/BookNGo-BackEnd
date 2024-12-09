package com.linuka.OnlineTicketing.websocket;

import com.linuka.OnlineTicketing.producerconsumer.TicketPool;
import org.springframework.stereotype.Component;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;

@Component
@ServerEndpoint("/ws/ticketing")
public class WebSocketServer implements TicketPool.TicketPoolListener {

    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();
    private static final TicketPool ticketPool = new TicketPool(200); // Initial ticket count

    static {
        ticketPool.setListener(new WebSocketServer());
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        sendTicketPoolStatus(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Handle incoming messages if needed
    }

    @Override
    public void onTicketCountChanged(int newCount) {
        sendTicketPoolStatusToAll();
    }

    private void sendTicketPoolStatus(Session session) {
        try {
            session.getBasicRemote().sendText("{\"ticketCount\": " + ticketPool.getTicketCount() + "}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTicketPoolStatusToAll() {
        for (Session session : sessions) {
            sendTicketPoolStatus(session);
        }
    }
}