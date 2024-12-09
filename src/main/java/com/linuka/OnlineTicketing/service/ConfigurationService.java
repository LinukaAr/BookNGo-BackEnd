package com.linuka.OnlineTicketing.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Properties;

@Service
public class ConfigurationService {
    private static final String CONFIG_FILE = "config.properties";

    public void saveConfiguration(int totalTickets, int ticketReleaseRate, int customerRetrievalRate, int maxTicketCapacity) {
        Properties properties = new Properties();
        properties.setProperty("totalTickets", String.valueOf(totalTickets));
        properties.setProperty("ticketReleaseRate", String.valueOf(ticketReleaseRate));
        properties.setProperty("customerRetrievalRate", String.valueOf(customerRetrievalRate));
        properties.setProperty("maxTicketCapacity", String.valueOf(maxTicketCapacity));

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Properties loadConfiguration() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}