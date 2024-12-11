import java.util.concurrent.locks.ReentrantLock;

// This class represents a vendor that releases tickets to the ticket pool at a fixed rate.
// Runnable class that will be used to create a thread for the vendor
public class Vendor implements Runnable {
    private final TicketPool ticketPool;
    private final int ticketReleaseRate;
    private final ReentrantLock lock;

    // Constructor for the Vendor class
    public Vendor(TicketPool ticketPool, int ticketReleaseRate, ReentrantLock lock) {
        this.ticketPool = ticketPool;
        this.ticketReleaseRate = ticketReleaseRate;
        this.lock = lock;
    }

    @Override
    // Run method that will be called when the thread is started
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { // While the thread is not interrupted
            lock.lock();
            try {
                ticketPool.addTickets(ticketReleaseRate); // Add tickets to the ticket pool
            } finally {
                lock.unlock();
            }
            try {
                Thread.sleep(1000); // Simulate time taken to release tickets
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}