package org.springframework.ai.openai.samples.helloworld.simple;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
	public Map<String, Object> question(@RequestParam(value = "topic", defaultValue = "tell me about shakespeare") String topic) {
		String userText = topic;
		String systemText = """
			You are a helpful AI assistant that helps people find information.
			You should reply to the user's request with just one word
			""";
		UserMessage userMessage = new UserMessage(userText);
		SystemMessage systemMessage = new SystemMessage(systemText);

		Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
		List<Generation> response = chatClient.call(prompt).getResults();
		return Map.of("generation", response);
	}
}
