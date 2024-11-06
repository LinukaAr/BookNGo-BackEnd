import java.net.URI;
import java.util.Scanner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class TicketingCLI {

    private static final String WS_URI = "ws://localhost:8080/your-websocket-endpoint";
    private WebSocketSession session;

    public TicketingCLI() {
        try {
            connectToWebSocket();
        } catch (Exception e) {
            System.err.println("Error connecting to WebSocket: " + e.getMessage());
        }
    }

    private void connectToWebSocket() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        session = client
                .doHandshake(new TextWebSocketHandler() {
                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                        System.out.println("Received from server: " + message.getPayload());
                    }
                }, URI.create(WS_URI))
                .get();
    }

    public void sendMessage(String message) throws Exception {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        } else {
            System.out.println("WebSocket session is not open.");
        }
    }

    public void closeConnection() throws Exception {
        if (session != null) {
            session.close();
        }
    }

    public static void main(String[] args) {
        TicketingCLI cli = new TicketingCLI();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Choose an option:");
            System.out.println("1. Add tickets (Vendor)");
            System.out.println("2. Purchase tickets (Customer)");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            try {
                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter number of tickets to add: ");
                        int ticketsToAdd = scanner.nextInt();
                        cli.sendMessage("{\"action\":\"add\",\"tickets\":" + ticketsToAdd + "}");
                    }
                    case 2 -> {
                        System.out.print("Enter number of tickets to purchase: ");
                        int ticketsToPurchase = scanner.nextInt();
                        cli.sendMessage("{\"action\":\"purchase\",\"tickets\":" + ticketsToPurchase + "}");
                    }
                    case 3 -> {
                        running = false;
                        cli.closeConnection();
                        System.out.println("Exiting...");
                    }
                    default -> System.out.println("Invalid choice, please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
