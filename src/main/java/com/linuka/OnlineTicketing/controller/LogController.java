package com.linuka.OnlineTicketing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @GetMapping
    public List<String> getLogs() throws IOException {
        // Use an absolute path
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("D:/IIT/2nd Year/oop/CW/BackEnd/OnlineTicketing/logs/log.txt"), StandardCharsets.UTF_8)) {
            return reader.lines().collect(Collectors.toList());
        }
    }
}