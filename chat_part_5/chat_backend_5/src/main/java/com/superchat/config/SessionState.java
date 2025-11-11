package com.superchat.config;

import com.superchat.model.AiAgentsPerSession;
import com.superchat.model.ClientProfile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class SessionState {

    private final ConcurrentHashMap<String, ClientProfile> profiles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AiAgentsPerSession> wirings = new ConcurrentHashMap<>();

    public ClientProfile getOrCreateProfile(String sessionId) {
        return profiles.computeIfAbsent(sessionId, id -> new ClientProfile());
    }

    public AiAgentsPerSession getOrCreateWiring(String sessionId, Supplier<AiAgentsPerSession> factory) {
        return wirings.computeIfAbsent(sessionId, id -> factory.get());
    }

    public void remove(String sessionId) {
        profiles.remove(sessionId);
        wirings.remove(sessionId);
    }
}