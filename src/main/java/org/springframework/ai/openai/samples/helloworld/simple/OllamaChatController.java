package org.springframework.ai.openai.samples.helloworld.simple;

import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
public class OllamaChatController {

    private final OllamaChatClient ollamaChatClient;
    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;

    @Autowired
    public OllamaChatController(OllamaChatClient chatClient, EmbeddingClient embeddingClient, VectorStore vectorStore) {
        this.ollamaChatClient = chatClient;
        this.embeddingClient = embeddingClient;
        this.vectorStore = vectorStore;
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

    @GetMapping("/ai/ollama/vector/add")
    public Map add() {
        List <Document> documents = List.of(new Document("Si tienes un archivo JSON con datos que deseas cargar en la base de datos vectorial, puedes usar la clase JsonReader de Spring AI para cargar campos específicos en el JSON, dividirlos en piezas pequeñas y luego pasar esas piezas pequeñas a la implementación de VectorStore.", 
        Map.of("title", "JSON", "author", "Other")));
        vectorStore.add(documents);
        return Map.of("add", "success");
    }

    @GetMapping("/ai/ollama/vector/search")
    public Map search(@RequestParam(value = "query", defaultValue = "archivo JSON") String query) {
        List <Document> documents = vectorStore.similaritySearch(SearchRequest.query(query));
        return Map.of("search", documents);
    }

    @GetMapping("/ai/ollama/vector/search_query")
    public Map search_query(@RequestParam(value = "query", defaultValue = "archivo JSON") String query,
                            @RequestParam(value = "k", defaultValue = "1") int topK,
                            @RequestParam(value = "threshold", defaultValue = "0.75") double threshold,
                            @RequestParam(value = "filter", defaultValue = "author == 'Other'") String filter) {
        List <Document> documents = vectorStore.similaritySearch(
            SearchRequest.defaults()
                .withQuery(query)
                .withTopK(topK)
                .withSimilarityThreshold(threshold)
                .withFilterExpression(filter));
        return Map.of("search", documents);
    }

    @DeleteMapping ("/ai/ollama/vector/delete")
    public Map delete( @RequestParam(value = "id") String id) {
        vectorStore.delete(List.of(id));
        return Map.of("delete", "success");
    }

}