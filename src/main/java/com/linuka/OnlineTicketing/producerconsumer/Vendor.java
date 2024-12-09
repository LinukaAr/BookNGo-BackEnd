package com.linuka.OnlineTicketing.producerconsumer;

import java.util.concurrent.locks.ReentrantLock;

public class Vendor implements Runnable {
    private final TicketPool ticketPool;
    private final int ticketReleaseRate;
    private final ReentrantLock lock;

    public Vendor(TicketPool ticketPool, int ticketReleaseRate, ReentrantLock lock) {
        this.ticketPool = ticketPool;
        this.ticketReleaseRate = ticketReleaseRate;
        this.lock = lock;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            lock.lock();
            try {
                ticketPool.addTickets(ticketReleaseRate);
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