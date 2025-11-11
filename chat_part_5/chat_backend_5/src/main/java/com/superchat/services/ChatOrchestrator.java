package com.superchat.services;

import com.superchat.model.AiAgentsPerSession;
import com.superchat.config.SessionState;
import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import com.superchat.model.ClientProfile;
import com.superchat.utils.AgentContextBuilder;
import com.superchat.websocket.dto.ChatMessageDTO;
import com.superchat.websocket.dto.ChatResponseDTO;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatOrchestrator {

    private final AiBootstrapService aiBootstrapService;                // infra compartida
    private final AiBuilderService aiBuilderService;      // factoría de agentes por sesión
    private final SessionState sessionState;
    private final AudienceSearcherService audienceSearcherService;
    private final ProductService productService;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void handle(ChatMessageDTO msg) {
        final String id = msg.getSessionId();
        final String text = msg.getText();
        final String topic = "/topic/chat/" + id;

        try {
            // typing
            messagingTemplate.convertAndSend(topic, ChatResponseDTO.builder()
                    .sessionId(id).type("typing").content("...").build());

            // === Wiring por sesión (memoria independiente por sessionId) ===
            AiAgentsPerSession wiringPerSession = sessionState.getOrCreateWiring(
                    id,
                    () -> aiBuilderService.buildPerSessionAgents(aiBootstrapService.getChatModel())
            );

            // === Perfil por sesión ===
            ClientProfile profile = sessionState.getOrCreateProfile(id);

            // Step 4 – Profile Extraction
            IProfileExtractionAgent extractor = wiringPerSession.getProfileExtractionAgent();
            String json = extractor.extractData(text)
                    .replace("```json", "")
                    .replace("```", "");
            profile.applyJson(json);

            // Step 5 – Semantic Search (usa infra compartida)
            EmbeddingModel model = aiBootstrapService.getEmbeddingModel();
            EmbeddingStore<TextSegment> store = aiBootstrapService.getEmbeddingStore();
            List<String> candidates = audienceSearcherService.findCandidateProductIds(profile, model, store, 7, 0.78);

            // Step 6 – Build Context (solo candidatos)
            String context = AgentContextBuilder.buildContextForAgent(
                    productService.findAllProducts(), candidates);

            // Step 7 – Chat Agent Response (memoria por sesión)
            IChatAgentA chat = wiringPerSession.getChatAgentA();
            String answer = chat.chat(text, context);

            messagingTemplate.convertAndSend(topic, ChatResponseDTO.builder()
                    .sessionId(id).type("assistant").content(answer).build());

        } catch (Exception e) {
            log.error("Chat error for session {}", id, e);
            messagingTemplate.convertAndSend(topic, ChatResponseDTO.builder()
                    .sessionId(id).type("error").content("Sorry, something went wrong.").build());
        }
    }
}