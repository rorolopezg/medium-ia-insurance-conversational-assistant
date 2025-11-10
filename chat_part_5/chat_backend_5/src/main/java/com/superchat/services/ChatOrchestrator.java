package com.superchat.services;

import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import com.superchat.model.ClientProfile;
import com.superchat.utils.AgentContextBuilder;
import com.superchat.websocket.dto.ChatMessageDTO;
import com.superchat.websocket.dto.ChatResponseDTO;
import com.superchat.websocket.session.SessionState;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestrator {

    private final AiBootstrap ai;
    private final SessionState sessions;
    private final AudienceSearcherService searcher;
    private final ProductService productService;
    private final SimpMessagingTemplate template;

    @Async
    public void handle(ChatMessageDTO msg) {
        final String id = msg.getSessionId();
        final String text = msg.getText();
        final String topic = "/topic/chat/" + id;

        try {
            // 0) feedback de "typing"
            template.convertAndSend(topic, ChatResponseDTO.builder()
                    .sessionId(id).type("typing").content("...").build());

            // Step 4 – Profile Extraction
            IProfileExtractionAgent extractor = ai.getWiring().getProfileExtractionAgent();
            String json = extractor.extractData(text)
                    .replace("```json", "")
                    .replace("```", "");
            ClientProfile profile = sessions.getOrCreate(id);
            profile.applyJson(json);

            // Step 5 – Semantic Search
            EmbeddingModel model = ai.getWiring().getEmbeddingModel();
            EmbeddingStore<TextSegment> store = ai.getWiring().getEmbeddingStore();
            List<String> candidates = searcher.findCandidateProductIds(profile, model, store, 7, 0.78);

            // Step 6 – Build Context (solo candidatos)
            String context = AgentContextBuilder.buildContextForAgent(
                    productService.findAllProducts(), candidates);

            // Step 7 – Chat Agent Response (puede invocar InsuranceQuoteTool)
            IChatAgentA chat = ai.getWiring().getChatAgentA();
            String answer = chat.chat(text, context);

            log.info("Chat answer for session {}: {}", id, answer);

            template.convertAndSend(topic, ChatResponseDTO.builder()
                    .sessionId(id).type("assistant").content(answer).build());

        } catch (Exception e) {
            log.error("Chat error for session {}", id, e);
            template.convertAndSend(topic, ChatResponseDTO.builder()
                    .sessionId(id).type("error")
                    .content("Sorry, something went wrong.").build());
        }
    }
}
