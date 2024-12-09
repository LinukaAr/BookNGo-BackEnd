package com.linuka.OnlineTicketing.service;

import com.linuka.OnlineTicketing.entity.Event;
import com.linuka.OnlineTicketing.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}