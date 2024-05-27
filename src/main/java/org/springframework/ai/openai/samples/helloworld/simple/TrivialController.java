package org.springframework.ai.openai.samples.helloworld.simple;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.models.TrivialAnswer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;
import java.util.List;

@RestController
public class TrivialController {
    

    private final OllamaApi oapi;

    private final String ROLE_SYSTEM_GENERATE = 
    """
        You are a helpful AI assistant, that only answers in just JSON format. 
        You make cards for a quiz game. 
        These cards have a question and 4 possible answers. 
        The answer should be always in json format as this: { \"question\": \"question\", \"language\":<the language used in the card> \"correct_answer\": \"correct answer\", \"incorrect_answers\": [\"incorrect answer 1\", \"incorrect answer 2\", \"incorrect answer 3\"] } 
        The code for the field language should be the language code, for example, for English is "en", for Spanish is "es", etc.
        In case that there are several cards, return them as an array of json objects.
    """;

    private final String ROLE_SYSTEM_CHECK = 
    """
        You are a helpful AI assistant, that only answers in just JSON format. 
        You check if the response of a quiz card is correct.
        You receive a json object with the question and the answer given by the user. This json should be always in json format as this: { \"question\": \"question\", \"answer\": \"answer\" }
        You respond with a json object qith the fields: "question", "answer" and "correct" with the value true or false.
    """;

    @Autowired
    public TrivialController() {
        this.oapi = new OllamaApi();

    }

    // get method for /ai/trivial/generate
    @GetMapping("/ai/trivial/generate")
    public Map generate(
        @RequestParam(value = "topic", defaultValue = "what's the capital of France") String topic,
        @RequestParam(value = "lang", defaultValue = "english") String lang) {
        OllamaApi oapi = new OllamaApi();
        var request = ChatRequest.builder("llama3")
            .withStream(false) // not streaming
            .withMessages(List.of(
                    Message.builder(Role.SYSTEM)
                        .withContent(ROLE_SYSTEM_GENERATE)
                        .build(),
                    Message.builder(Role.USER)
                        .withContent(topic + " in " + lang + " language")
                        .build()))
            .withOptions(OllamaOptions.create().withTemperature(0.5f))
            .build();
        return Map.of("generation", oapi.chat(request).message().content());
    }

    @GetMapping("/ai/trivial/random")
    public Map random(
        @RequestParam(value = "number", defaultValue = "1") int number,
        @RequestParam(value = "lang", defaultValue = "english") String lang) {
        String topic = """ 
            make {number} questions about random topics in {lang} language
            """;
        topic = new PromptTemplate(topic).render(Map.of("number", number, "lang", lang));
        var request = ChatRequest.builder("llama3")
            .withStream(false) // not streaming
            .withMessages(List.of(
                    Message.builder(Role.SYSTEM)
                        .withContent(ROLE_SYSTEM_GENERATE)
                        .build(),
                    Message.builder(Role.USER)
                        .withContent(topic)
                        .build()))
            .withOptions(OllamaOptions.create().withTemperature(0.9f))
            .build();
        return Map.of("generation", oapi.chat(request).message().content().indent(2));
    }

    // post method for /ai/trivial/check 
    // get json object with question and answer from body
    @PostMapping(
        value="/ai/trivial/check", 
        consumes = {MediaType.APPLICATION_JSON_VALUE}, 
        produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map check( @RequestBody TrivialAnswer body) {

        String requestString = "{ \"question\": \"" + body.question + "\", \"answer\": \"" + body.answer + "\" }";
        OllamaApi oapi = new OllamaApi();
        var request = ChatRequest.builder("llama3")
            .withStream(false) // not streaming
            .withMessages(List.of(
                    Message.builder(Role.SYSTEM)
                        .withContent(ROLE_SYSTEM_CHECK)
                        .build(),
                    Message.builder(Role.USER)
                        .withContent(requestString)
                        .build()))
            .withOptions(OllamaOptions.create().withTemperature(0.5f))
            .build();
        return Map.of("generation", oapi.chat(request).message().content());
    }

}
