package com.superchat.websocket.session;

import com.superchat.model.ClientProfile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionState {

    private final ConcurrentHashMap<String, ClientProfile> profiles = new ConcurrentHashMap<>();

    public ClientProfile getOrCreate(String sessionId) {
        return profiles.computeIfAbsent(sessionId, id -> new ClientProfile());
    }

    public void clear(String sessionId) {
        profiles.remove(sessionId);
    }
}
