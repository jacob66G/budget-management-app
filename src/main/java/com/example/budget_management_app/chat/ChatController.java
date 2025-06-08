package com.example.budget_management_app.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/response")
    public ResponseEntity<String> getResponse(@RequestParam(name = "message", defaultValue = "None")
                                                  String message){

        return ResponseEntity
                .ok(this.chatService.generateResponse(message));
    }
}
