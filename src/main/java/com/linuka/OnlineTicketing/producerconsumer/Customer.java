package com.linuka.OnlineTicketing.producerconsumer;

import java.util.concurrent.locks.ReentrantLock;

public class Customer implements Runnable {
    private final TicketPool ticketPool;
    private final int customerRetrievalRate;
    private final ReentrantLock lock;

    public Customer(TicketPool ticketPool, int customerRetrievalRate, ReentrantLock lock) {
        this.ticketPool = ticketPool;
        this.customerRetrievalRate = customerRetrievalRate;
        this.lock = lock;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            lock.lock();
            try {
                ticketPool.removeTicket(customerRetrievalRate);
                System.out.println(Thread.currentThread().getName() + " purchased " + customerRetrievalRate + " tickets.");
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