package com.example.rag.langchain4j.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SupportController {
    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/chat")
    public String generateMessage(@RequestParam (value = "message") String message) {
        return supportService.chat(message);
    }
}
