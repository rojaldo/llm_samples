package org.springframework.ai.openai.samples.helloworld.simple;

import java.util.Map;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class OllamaChatController {

    private final OllamaChatClient ollamaChatClient;
    private final EmbeddingClient embeddingClient;



    @Autowired
    public OllamaChatController(OllamaChatClient chatClient, EmbeddingClient embeddingClient) {
        this.ollamaChatClient = chatClient;
        this.embeddingClient = embeddingClient;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", ollamaChatClient.call(message));
    }


    @GetMapping("/ai/ollama/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingClient.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }

    @GetMapping("/ai/ollama/embedding/compare")
    public Map compare(@RequestParam(value = "m1", defaultValue = "dog") String message1,
                       @RequestParam(value = "m2", defaultValue = "cat") String message2) {
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

    // @GetMapping("/ai/generateStream")
	// public Object generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
    //     Prompt prompt = new Prompt(new UserMessage(message));
    //     return ollamaChatClient.stream(prompt);
    // }

}