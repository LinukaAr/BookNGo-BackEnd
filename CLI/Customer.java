import java.util.concurrent.locks.ReentrantLock;

// Customer class implements Runnable to allow instances to be executed by a thread
public class Customer implements Runnable {
    protected final TicketPool ticketPool;
    protected final int customerRetrievalRate;
    protected final ReentrantLock lock;

        // Constructor to initialize the Customer with a ticket pool, retrieval rate, and lock
    public Customer(TicketPool ticketPool, int customerRetrievalRate, ReentrantLock lock) {
        this.ticketPool = ticketPool;
        this.customerRetrievalRate = customerRetrievalRate;
        this.lock = lock;
    }

    // The run method is executed when the thread starts
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {//Loop until the thread is interrupted

            lock.lock();
            try {
                ticketPool.removeTicket(customerRetrievalRate);
                String logMessage = Thread.currentThread().getName() + " purchased " + customerRetrievalRate + " tickets.";
                System.out.println(logMessage);
            } finally {
                lock.unlock();
            }
            try {
                Thread.sleep(1000); // Simulate time taken to purchase tickets
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}