package com.superchat.websocket.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDTO {
    private String sessionId;  // eco para el cliente
    private String type;       // "assistant", "typing", "error"
    private String content;    // respuesta del agente
}
