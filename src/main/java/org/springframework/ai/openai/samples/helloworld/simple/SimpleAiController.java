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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.models.Actor;
import org.springframework.models.Movie;

// @RestController
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
    public Actor generate(@RequestParam(value = "actor", defaultValue = "Jeff Bridges") String actor) {
        var outputParser = new BeanOutputParser<>(Actor.class);

        String userMessage =
                """
                Generate the filmography for the actor {actor}. Include all the movies until 2022
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(userMessage, Map.of("actor", actor, "format", outputParser.getFormat() ));
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();

        Actor actorsFilms = outputParser.parse(generation.getOutput().getContent());
        return actorsFilms;
    }

	@GetMapping("/ai/last_movie")
    public Movie lastMovie(@RequestParam(value = "actor", defaultValue = "Jeff Bridges") String actorName) {
        var outputParser = new BeanOutputParser<>(Actor.class);

        String userMessage =
                """
                Generate the filmography for the actor {actor}. Include all the movies until 2022. Order the movies by release date. Include those movies that {actor} is not the main actor in.
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(userMessage, Map.of("actor", actorName, "format", outputParser.getFormat() ));
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();

        Actor actor = outputParser.parse(generation.getOutput().getContent());
        return actor.movies.get(actor.movies.size() - 1);
    }

	@GetMapping("/ai/next_movie")
	public Object nextMovie(@RequestParam(value = "movie", defaultValue = "Pulp Fiction") String movieName) {
		var outputParser = new BeanOutputParser<>(List.class);

		String userMessage =
				"""
				Give me an array with the actors that are going to be in the next movie of {movieName}
				""";

		PromptTemplate promptTemplate = new PromptTemplate(userMessage, Map.of("movieName", movieName, "format", outputParser.getFormat() ));
		Prompt prompt = promptTemplate.create();
		Generation generation = chatClient.call(prompt).getResult();

		List actors = outputParser.parse(generation.getOutput().getContent());

		String secondMessage =
				"""
				give me a json array of only the next movie that appeared each actor from {actors} after {movieName}. If there is not next movie, say no next movie. With the format: actor_name, movie_name, year
				""";
		promptTemplate = new PromptTemplate(secondMessage, Map.of("actors", actors, "movieName", movieName, "format", outputParser.getFormat() ));
		prompt = promptTemplate.create();
		generation = chatClient.call(prompt).getResult();
		List movies = outputParser.parse(generation.getOutput().getContent());
		return movies;
	}

	@GetMapping("/ai/weather")
	public Object weather(@RequestParam(value = "city", defaultValue = "New York") String city) {
		return new MockWeatherService().apply(new MockWeatherService.Request(city, MockWeatherService.Unit.C));
	}

	@Configuration
	static class Config {

		@Bean
		@Description("Get the weather in location") // function description
			public Function<MockWeatherService.Request, MockWeatherService.Response> weatherFunction1() {
			return new MockWeatherService();
		}

}
}


