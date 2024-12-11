import java.io.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingCLI {
    // Instance variables
    private int totalTickets;
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;
    private final ReentrantLock lock = new ReentrantLock();
    private TicketPool ticketPool;
    private static final String CONFIG_FILE = "config.properties";

    // Method to configure the system
    private void configureSystem() {
        Properties properties = new Properties();
        File configFile = new File(CONFIG_FILE);
        Scanner scanner = new Scanner(System.in);

        // Check if the configuration file exists
        if (configFile.exists()) {
            System.out.print("Configuration file found. Do you want to load it? (yes/no): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            
            // If user chooses to load the configuration file
            if (choice.equals("yes")) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);  // Load properties from the file
                    totalTickets = Integer.parseInt(properties.getProperty("totalTickets", "0"));
                    ticketReleaseRate = Integer.parseInt(properties.getProperty("ticketReleaseRate", "0"));
                    customerRetrievalRate = Integer.parseInt(properties.getProperty("customerRetrievalRate", "0"));
                    maxTicketCapacity = Integer.parseInt(properties.getProperty("maxTicketCapacity", "0"));
                    
                    // Print loaded configuration
                    System.out.println("Configuration loaded from " + CONFIG_FILE);
                    System.out.println("Total Tickets: " + totalTickets);
                    System.out.println("Ticket Release Rate: " + ticketReleaseRate);
                    System.out.println("Customer Retrieval Rate: " + customerRetrievalRate);
                    System.out.println("Max Ticket Capacity: " + maxTicketCapacity);

                    ticketPool = new TicketPool(maxTicketCapacity);// Create a new ticket pool
                    ticketPool.addTickets(totalTickets);// Add tickets to the ticket pool
                    return;
                } catch (IOException | NumberFormatException e) {
                    System.out.println("Error loading configuration. Proceeding with manual input.");
                }
            }
        }

        //Menu to manually configure the system
        System.out.println("Welcome to the Ticketing System!");
        totalTickets = getValidInput(scanner, "Enter total tickets: ");
        ticketReleaseRate = getValidInput(scanner, "Enter ticket release rate: ");
        customerRetrievalRate = getValidInput(scanner, "Enter customer retrieval rate: ");
        maxTicketCapacity = getValidInput(scanner, "Enter max ticket capacity: ");

        saveConfiguration();// Save the configuration to a file
        ticketPool = new TicketPool(maxTicketCapacity);
        ticketPool.addTickets(totalTickets);
    }

    // Method to get a valid positive integer input from the user
    private int getValidInput(Scanner scanner, String prompt) {
        int input;
        while (true) {// Loop until valid input is received
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                if (input > 0) {
                    break;
                }
            } else {
                scanner.next();
            }
            System.out.println("Invalid input. Please enter a positive integer.");
        }
        return input;
    }

    // Method to save the configuration to a file
    private void saveConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("totalTickets", String.valueOf(totalTickets));
        properties.setProperty("ticketReleaseRate", String.valueOf(ticketReleaseRate));
        properties.setProperty("customerRetrievalRate", String.valueOf(customerRetrievalRate));
        properties.setProperty("maxTicketCapacity", String.valueOf(maxTicketCapacity));

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {// Save the properties to the file
            properties.store(fos, "Ticketing System Configuration");
            System.out.println("Configuration saved to " + CONFIG_FILE);
        } catch (IOException e) {// Handle file IO exceptions
            System.out.println("Error saving configuration: " + e.getMessage());
        }
    }

    // Method to start the ticket handling operations
    public void start() {
        System.out.println("Ticket handling operations started.");
        initializeVendorsAndCustomers();
    }

    // Method to stop the ticket handling operations
    public void stop() {
        System.out.println("Ticket handling operations stopped.");
    }

    // Method to add tickets to the ticket pool
    public void addTickets(int count) {
        lock.lock();// Lock the critical section
        try {
            ticketPool.addTickets(count);
            logTransaction(count + " tickets added.");
            System.out.println("{\"action\": \"add\", \"tickets\": " + count + "}");
        } finally {
            lock.unlock();
        }
    }

    // Method to log a transaction
    private void logTransaction(String message) {
        System.out.println(message);
    }

    // Method to initialize vendors and customers
    private void initializeVendorsAndCustomers() {
        Vendor[] vendors = new Vendor[10];
        Thread[] vendorThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            vendors[i] = new Vendor(ticketPool, ticketReleaseRate, lock);
            vendorThreads[i] = new Thread(vendors[i], "Vendor-" + (i + 1));
            vendorThreads[i].start();
        }

        // Create customer threads
        Customer[] customers = new Customer[10];// Create an array of customer objects
        Thread[] customerThreads = new Thread[10];// Create an array of customer threads
        for (int i = 0; i < 10; i++) {
            customers[i] = new Customer(ticketPool, customerRetrievalRate, lock);
            customerThreads[i] = new Thread(customers[i], "Customer-" + (i + 1));
            customerThreads[i].start();
        }
    }

    // Main method
    public static void main(String[] args) {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        TicketingCLI cli = new TicketingCLI();
        cli.configureSystem();
        Scanner scanner = new Scanner(System.in);
        String command;
        while (true) {
            System.out.print("Enter command (start/stop/add/exit): ");
            command = scanner.nextLine();
            if (command.equalsIgnoreCase("start")) {
                cli.start();
            } else if (command.equalsIgnoreCase("stop")) {
                cli.stop();
            } else if (command.equalsIgnoreCase("add")) {
                System.out.print("Enter number of tickets to add: ");
                int count = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                cli.addTickets(count);
            } else if (command.equalsIgnoreCase("exit")) {
                cli.stop();
                break;
            } else {
                System.out.println("Invalid command.");
            }
        }
        scanner.close();
    }
}