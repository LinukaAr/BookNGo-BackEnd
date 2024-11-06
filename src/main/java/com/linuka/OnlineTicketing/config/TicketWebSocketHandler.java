import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TicketWebSocketHandler extends TextWebSocketHandler {

    private int ticketPool = 100;  // Example starting ticket count
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload());
        String action = jsonMessage.get("action").asText();
        int tickets = jsonMessage.get("tickets").asInt();

        if ("add".equals(action)) {
            ticketPool += tickets;
            session.sendMessage(new TextMessage("Added " + tickets + " tickets. Total now: " + ticketPool));
        } else if ("purchase".equals(action)) {
            if (ticketPool >= tickets) {
                ticketPool -= tickets;
                session.sendMessage(new TextMessage("Purchased " + tickets + " tickets. Total now: " + ticketPool));
            } else {
                session.sendMessage(new TextMessage("Not enough tickets available. Only " + ticketPool + " left."));
            }
        }
    }
}
