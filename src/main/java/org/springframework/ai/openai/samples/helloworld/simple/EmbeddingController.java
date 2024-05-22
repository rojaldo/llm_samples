package org.springframework.ai.openai.samples.helloworld.simple;

import java.util.List;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties.Embedded;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @RestController
public class EmbeddingController {

    private final EmbeddingClient embeddingClient;

    @Autowired
    
    public EmbeddingController(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingClient.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }

    @GetMapping("/ai/embedding/similarity")
    public Map similarity(@RequestParam(value = "m1", defaultValue = "mother") String message1,
                          @RequestParam(value = "m2", defaultValue = "madre") String message2) {
        List<Double> embedding1 = embeddingClient.embed(message1);
        List<Double> embedding2 = embeddingClient.embed(message2);
        double similarity = cosineSimilarity(embedding1, embedding2);
        return Map.of("similarity", similarity);
    }

    double cosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += Math.pow(embedding1.get(i), 2);
            norm2 += Math.pow(embedding2.get(i), 2);
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}