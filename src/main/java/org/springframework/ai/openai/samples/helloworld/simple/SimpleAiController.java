package org.springframework.ai.openai.samples.helloworld.simple;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import org.springframework.models.ActorsFilms;

@RestController
public class SimpleAiController {

	private final ChatClient chatClient;

	@Autowired
	public SimpleAiController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/ai/simple")
	public Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return Map.of("generation", chatClient.call(message));
	}

	@GetMapping("/ai/joke")
	public Map<String, String> joke(@RequestParam(value = "topic", defaultValue = "animals") String topic) {
		PromptTemplate promptTemplate = new PromptTemplate("tell me a joke about {topic}");
		String message = promptTemplate.render(Map.of("topic", topic));
		return Map.of("generation", chatClient.call(message));
	}

	@GetMapping("/ai/question")
	public Map<String, Object> question(
		@RequestParam(value = "topic", defaultValue = "tell me about shakespeare") String topic,
		@RequestParam(value = "words", defaultValue = "10") int words) {
		String userText = topic;
		int wordsNumber = words;
		String systemText = """
			You are a helpful AI assistant that helps people find information.
			You should reply to the user's request with just {wordsNumber} word
			""";
		UserMessage userMessage = new UserMessage(userText);
		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
		Message systemMessage = systemPromptTemplate.createMessage(Map.of("wordsNumber", wordsNumber));

		Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
		List<Generation> response = chatClient.call(prompt).getResults();
		return Map.of("generation", response);
	}

	@GetMapping("/ai/movies")
    public ActorsFilms generate(@RequestParam(value = "actor", defaultValue = "Jeff Bridges") String actor) {
        var outputParser = new BeanOutputParser<>(ActorsFilms.class);

        String userMessage =
                """
                Generate the filmography for the actor {actor}.
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(userMessage, Map.of("actor", actor, "format", outputParser.getFormat() ));
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();

        ActorsFilms actorsFilms = outputParser.parse(generation.getOutput().getContent());
        return actorsFilms;
    }
}


