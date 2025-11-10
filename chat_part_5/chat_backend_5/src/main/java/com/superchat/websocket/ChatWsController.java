package com.superchat.websocket;

import com.superchat.services.ChatOrchestrator;
import com.superchat.websocket.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatOrchestrator orchestrator;

    @MessageMapping("/chat.send")
    public void onMessage(ChatMessageDTO msg) {
        orchestrator.handle(msg);
    }
}
