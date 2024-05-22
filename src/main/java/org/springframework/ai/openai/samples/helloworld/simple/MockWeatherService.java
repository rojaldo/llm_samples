package org.springframework.ai.openai.samples.helloworld.simple;

import java.util.function.Function;

import org.springframework.ai.openai.samples.helloworld.simple.MockWeatherService.Request;
import org.springframework.ai.openai.samples.helloworld.simple.MockWeatherService.Response;

public class MockWeatherService implements Function<Request, Response> {

	public enum Unit { C, F }
	public record Request(String location, Unit unit) {}
	public record Response(double temp, Unit unit) {}

	public Response apply(Request request) {
		return new Response(30.0, Unit.C);
	}
}