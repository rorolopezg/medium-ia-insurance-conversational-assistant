package com.superchat.websocket.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private String sessionId;  // UUID generado por el front
    private String text;       // mensaje del usuario
}
