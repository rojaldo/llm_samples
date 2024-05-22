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

    // @GetMapping("/ai/generateStream")
	// public Object generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
    //     Prompt prompt = new Prompt(new UserMessage(message));
    //     return ollamaChatClient.stream(prompt);
    // }

}