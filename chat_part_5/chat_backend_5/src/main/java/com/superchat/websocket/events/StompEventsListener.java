package com.superchat.websocket.events;

import com.superchat.config.SessionState;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class StompEventsListener {

    private final SessionState sessions;

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        // Aquí puedes mapear el STOMP sessionId a tu sessionId de aplicación, si los correlacionas.
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String stompSessionId = sha.getSessionId();
        // Si tienes un mapa stompSessionId -> appSessionId, úsalo para limpiar:
        sessions.remove(stompSessionId);
    }
}