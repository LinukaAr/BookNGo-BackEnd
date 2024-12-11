package com.linuka.OnlineTicketing.producerconsumer;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

//implements Runnable interface
public class Customer implements Runnable {
    protected final TicketPool ticketPool;
    protected final int customerRetrievalRate;
    protected final ReentrantLock lock;
    protected WebSocketSession session;

    //constructor
    public Customer(TicketPool ticketPool, int customerRetrievalRate, ReentrantLock lock, WebSocketSession session) {
        this.ticketPool = ticketPool;
        this.customerRetrievalRate = customerRetrievalRate;
        this.lock = lock;
        this.session = session;
    }
    //constructor
    public Customer(TicketPool ticketPool, int customerRetrievalRate, ReentrantLock lock) {
        this.ticketPool = ticketPool;
        this.customerRetrievalRate = customerRetrievalRate;
        this.lock = lock;

    }


    @Override
    //run method for the thread
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            lock.lock();//lock the thread 
            try {
                ticketPool.removeTicket(customerRetrievalRate); //remove tickets from the pool
                String logMessage = Thread.currentThread().getName() + " purchased " + customerRetrievalRate + " tickets.";
                System.out.println(logMessage);
                try {
                    session.sendMessage(new TextMessage("{\"message\": \"" + logMessage + "\"}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                lock.unlock();//unlock the thread
            }
            try {
                Thread.sleep(1000); // Simulate time taken to purchase tickets
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}