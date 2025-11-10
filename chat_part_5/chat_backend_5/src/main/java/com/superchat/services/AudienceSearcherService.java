package com.superchat.services;

import com.superchat.model.ClientProfile;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.superchat.services.ProductFieldsUtil.*;

@Slf4j
@Service
public final class AudienceSearcherService {
    private AudienceSearcherService(){}

    public List<String> findCandidateProductIds(ClientProfile profile,
                                                       EmbeddingModel embeddingModel,
                                                       EmbeddingStore<TextSegment> store,
                                                       int maxResults,
                                                       double minScore) {

        StringBuffer logMessage = new StringBuffer();
        // 1) Build query for target audience
        String audienceQuery = profile.friendlyProfileDescription();
        Embedding queryForEmbeddings = embeddingModel.embed(audienceQuery).content();

        // 2) Search semantically in the store
        EmbeddingSearchRequest req = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryForEmbeddings)
                .maxResults(maxResults * 2) // Get more to allow for post-filtering
                .minScore(minScore)
                .build();

        EmbeddingSearchResult<TextSegment> res = store.search(req);

        // Log initial matches:
        logMessage.append("Found %s initial matches for audience query (semantic search).".formatted(res.matches().size()));
        res.matches().stream().forEach(match -> {
            logMessage.append("{");
            logMessage.append("PRODUCT ID: ");
            logMessage.append(match.embedded().metadata().getString(META_PRODUCT_ID));
            logMessage.append(", PRODUCT NAME: '");
            logMessage.append(match.embedded().metadata().getString(META_PRODUCT_NAME));
            logMessage.append("'} ");

        });
        log.info(logMessage.toString());

        // 3) Post-filter: only "audience" segments and by age range:
        Integer age = profile.getAge() == null ? 0 : profile.getAge();
        List<EmbeddingMatch<TextSegment>> audienceMatches = res.matches().stream()
                .filter(m ->
                        SEG_AUDIENCE.equals(String.valueOf(m.embedded().metadata().getString(META_SEGMENT_TYPE))))
                .filter(m -> {
                    Integer min = m.embedded().metadata().getInteger(META_AGE_MIN);
                    Integer max = m.embedded().metadata().getInteger(META_AGE_MAX);
                    return age >= min && age <= max;
                })
                .toList();
        // Log post-filtered matches:
        logMessage.setLength(0);
        logMessage.append("After post-filtering, %s matches remain for audience query.".formatted(audienceMatches.size()));
        audienceMatches.stream().forEach(match -> {
            logMessage.append("{");
            logMessage.append("PRODUCT ID: ");
            logMessage.append(match.embedded().metadata().getString(META_PRODUCT_ID));
            logMessage.append(", PRODUCT NAME: ");
            logMessage.append(match.embedded().metadata().getString(META_PRODUCT_NAME));
            logMessage.append("} ");

        });
        log.info(logMessage.toString());

        if (audienceMatches.isEmpty()) {
            return Collections.emptyList();
        }

        // 4) Consolidate by productId → keep the best score per product:
        Map<String, EmbeddingMatch<TextSegment>> bestPerProduct =
                audienceMatches.stream().collect(Collectors.toMap(
                        m -> String.valueOf(m.embedded().metadata().getString(META_PRODUCT_ID)),
                        m -> m,
                        (m1, m2) -> m1.score() >= m2.score() ? m1 : m2
                ));

        // Log consolidated matches:
        logMessage.setLength(0);
        logMessage.append("After consolidate, %s matches remain for audience query.".formatted(bestPerProduct.size()));
        //Log PRODUCT ID and PRODUCT NAME:
        bestPerProduct.values().forEach(m -> {
            logMessage.append("{");
            logMessage.append("PRODUCT ID: ");
            logMessage.append(m.embedded().metadata().getString(META_PRODUCT_ID));
            logMessage.append(", PRODUCT NAME: ");
            logMessage.append(m.embedded().metadata().getString(META_PRODUCT_NAME));
            logMessage.append("} ");
        });
        log.info(logMessage.toString());

        // 5) Sort by score desc and cut to maxResults:
        logMessage.setLength(0);
        List<EmbeddingMatch<TextSegment>> top = bestPerProduct.values().stream()
                .sorted(Comparator.comparingDouble(EmbeddingMatch<TextSegment>::score).reversed())
                .limit(maxResults)
                .toList();

        // Log top matches:
        logMessage.setLength(0);
        top.forEach(
                m -> {
                    logMessage.append("{");
                    logMessage.append("PRODUCT ID: ");
                    logMessage.append(m.embedded().metadata().getString(META_PRODUCT_ID));
                    logMessage.append(", PRODUCT NAME: ");
                    logMessage.append(m.embedded().metadata().getString(META_PRODUCT_NAME));
                    logMessage.append(", SCORE: ");
                    logMessage.append(m.score());
                    logMessage.append("} ");
                }
        );
        log.debug("Top candidates {}", logMessage);

        return top.stream()
                .map(m -> m.embedded().metadata().getString(META_PRODUCT_ID))
                .toList();
    }
}
