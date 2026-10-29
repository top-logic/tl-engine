/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.tl.ai.core.tools.web.RecordingServer.Reply;

import junit.framework.TestCase;

/**
 * What {@link BraveSearchEngine} and {@link TavilySearchEngine} send, and how they wait out a rate
 * limit, against a local stub of the search API.
 *
 * <p>
 * The waits are recorded instead of slept, so a test that asks for a one-hour wait takes no time.
 * </p>
 */
public class TestSearchEngineRequests extends TestCase {

	private static final String BRAVE_OK = "{\"web\":{\"results\":["
		+ "{\"title\":\"T1\",\"url\":\"https://a.example/1\",\"description\":\"one\"}]}}";

	private static final String TAVILY_OK = "{\"results\":["
		+ "{\"title\":\"T1\",\"url\":\"https://a.example/1\",\"content\":\"one\"}]}";

	private final List<Duration> _waits = Collections.synchronizedList(new ArrayList<>());

	/** Count, age and language reach Brave as its own parameters. */
	public void testBraveSendsCountFreshnessAndLanguage() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(BRAVE_OK))) {
			WebSearchResults results = brave(api).search(new WebSearchQuery("solar panels", List.of(), List.of(),
				12, Freshness.WEEK, "ja"));

			assertEquals(1, results.hits().size());
			String request = api.requestLines().get(0);
			assertTrue(request, request.contains("q=solar+panels"));
			assertTrue(request, request.contains("&count=12"));
			assertTrue(request, request.contains("&freshness=pw"));
			assertTrue("Brave names Japanese jp: " + request, request.contains("&search_lang=jp"));
		}
	}

	/** Without age and language, Brave gets neither parameter. */
	public void testBraveSendsNoFilterThatWasNotAsked() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(BRAVE_OK))) {
			brave(api).search(WebSearchQuery.of("q", 5));

			String request = api.requestLines().get(0);
			assertFalse(request, request.contains("freshness"));
			assertFalse(request, request.contains("search_lang"));
		}
	}

	/** A 429 with {@code Retry-After} is waited out for exactly that long, then the search succeeds. */
	public void testBraveWaitsOutRetryAfter() throws Exception {
		try (RecordingServer api = RecordingServer.start(sequence(
				rateLimited(List.of("Retry-After: 2")),
				json(BRAVE_OK)))) {
			WebSearchResults results = brave(api).search(WebSearchQuery.of("q", 5));

			assertEquals(1, results.hits().size());
			assertEquals(2, api.contactCount());
			assertWaits(2000);
		}
	}

	/** {@code Retry-After} may be a date; the wait is the time until it. */
	public void testRetryAfterAsADate() throws Exception {
		String inFourSeconds = DateTimeFormatter.RFC_1123_DATE_TIME
			.format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(4));
		try (RecordingServer api = RecordingServer.start(sequence(
				rateLimited(List.of("Retry-After: " + inFourSeconds)),
				json(BRAVE_OK)))) {
			brave(api).search(WebSearchQuery.of("q", 5));

			assertEquals(1, _waits.size());
			long seconds = _waits.get(0).toSeconds();
			assertTrue("about four seconds: " + _waits, seconds >= 2 && seconds <= 4);
		}
	}

	/**
	 * Brave says which of its windows is exhausted. The per-second window resets in one second, so
	 * one second is waited - not the rest of the month the second window would take.
	 */
	public void testBraveWaitsForTheExhaustedWindowOnly() throws Exception {
		try (RecordingServer api = RecordingServer.start(sequence(
				rateLimited(List.of("X-RateLimit-Remaining: 0, 1999", "X-RateLimit-Reset: 1, 1419200")),
				json(BRAVE_OK)))) {
			brave(api).search(WebSearchQuery.of("q", 5));

			assertWaits(1000);
		}
	}

	/**
	 * Workers refused together are told the same reset time and would collide again. Once an answer
	 * names the plan's rate ({@code 1;w=1}), every request gets a slot of its own, a little more than one
	 * second after the one before - also a new search that was never refused.
	 */
	public void testTheRateOfAPlanPacesLaterRequests() throws Exception {
		try (RecordingServer api = RecordingServer.start(sequence(
				rateLimited(List.of("X-RateLimit-Policy: 1;w=1, 2000;w=2678400",
					"X-RateLimit-Remaining: 0, 1999", "X-RateLimit-Reset: 1, 1419200")),
				json(BRAVE_OK)))) {
			BraveSearchEngine engine = brave(api);
			engine.search(WebSearchQuery.of("first", 5));
			engine.search(WebSearchQuery.of("second", 5));
			engine.search(WebSearchQuery.of("third", 5));

			// The sleeper does not sleep, so the clock has hardly moved: each slot lies the plan's
			// second and a tenth after the one before, counted from the retry's slot.
			assertWaits(1000, 2100, 3200);
			assertEquals(4, api.contactCount());
		}
	}

	/**
	 * An exhausted monthly quota asks for days. That is not waited for, because the run waits with
	 * it; the search fails at once with a sentence that tells the model to stop searching.
	 */
	public void testAQuotaIsNotWaitedFor() throws Exception {
		try (RecordingServer api = RecordingServer.start(sequence(
				rateLimited(List.of("X-RateLimit-Remaining: 0, 0", "X-RateLimit-Reset: 1, 86400"))))) {
			try {
				brave(api).search(WebSearchQuery.of("q", 5));
				fail("A quota cannot be waited out.");
			} catch (IOException expected) {
				assertEquals("Brave search is rate-limited (HTTP 429) and asks to wait 86400 seconds before the"
					+ " next request. Do not search again in this run.", expected.getMessage());
			}
			assertEquals(1, api.contactCount());
			assertTrue(_waits.isEmpty());
		}
	}

	/**
	 * Without a hint the waits double from one second; after the last retry the search fails with a
	 * sentence, not with the API's error body.
	 */
	public void testWithoutAHintTheBackoffDoublesAndEnds() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> rateLimited(List.of()))) {
			try {
				brave(api).search(WebSearchQuery.of("q", 5));
				fail("The API never answers.");
			} catch (IOException expected) {
				assertEquals("Brave search is rate-limited (HTTP 429) and still was after 3 retries."
					+ " Wait before you search again.", expected.getMessage());
			}
			assertEquals(1 + SearchHttp.MAX_RETRIES, api.contactCount());
			assertEquals(SearchHttp.MAX_RETRIES, _waits.size());
			for (int i = 0; i < _waits.size(); i++) {
				long expected = 1000L << i;
				long actual = _waits.get(i).toMillis();
				assertTrue("wait " + i + " is " + actual + " ms", actual >= expected && actual < expected + 250);
			}
		}
	}

	/**
	 * Brave names its rate on every answer, so the pace holds before the first refusal: a second
	 * search waits for its slot instead of being refused.
	 */
	public void testASuccessfulAnswerSetsThePaceToo() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> new Reply(200, "OK",
				List.of("Content-Type: application/json", "X-RateLimit-Policy: 1;w=1, 2000;w=2678400"), BRAVE_OK))) {
			BraveSearchEngine engine = brave(api);
			engine.search(WebSearchQuery.of("first", 5));
			engine.search(WebSearchQuery.of("second", 5));

			assertEquals(2, api.contactCount());
			assertEquals("only the second search waits: " + _waits, 1, _waits.size());
			long millis = _waits.get(0).toMillis();
			assertTrue("for about the plan's second and a tenth: " + _waits, millis > 1000 && millis <= 1100);
		}
	}

	/** A temporary server failure is retried like a rate limit. */
	public void testAServerFailureIsRetried() throws Exception {
		try (RecordingServer api = RecordingServer.start(sequence(
				new Reply(503, "Service Unavailable", List.of("Retry-After: 1"), "busy"),
				json(TAVILY_OK)))) {
			WebSearchResults results = tavily(api).search(WebSearchQuery.of("q", 5));

			assertEquals(1, results.hits().size());
			assertWaits(1000);
		}
	}

	/** Any other error is not retried. */
	public void testAClientErrorIsNotRetried() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> new Reply(422, "Unprocessable", List.of(),
				"{\"error\":\"bad search_lang\"}"))) {
			try {
				brave(api).search(WebSearchQuery.of("q", 5));
				fail("422 is an error.");
			} catch (IOException expected) {
				assertTrue(expected.getMessage(), expected.getMessage().contains("HTTP 422"));
			}
			assertEquals(1, api.contactCount());
		}
	}

	/** Tavily gets the count as {@code max_results} and the age as {@code time_range}. */
	public void testTavilySendsCountAndTimeRange() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(TAVILY_OK))) {
			tavily(api).search(new WebSearchQuery("q", List.of(), List.of(), 7, Freshness.MONTH, null));

			String body = api.bodies().get(0);
			assertTrue(body, body.contains("\"max_results\":7"));
			assertTrue(body, body.contains("\"time_range\":\"month\""));
		}
	}

	/** Without an age, Tavily gets no {@code time_range} at all - not an empty one. */
	public void testTavilySendsNoTimeRangeThatWasNotAsked() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(TAVILY_OK))) {
			tavily(api).search(WebSearchQuery.of("q", 5));

			assertFalse(api.bodies().get(0), api.bodies().get(0).contains("time_range"));
		}
	}

	/**
	 * Checks the recorded waits: each is the given number of milliseconds, plus the jitter of at most
	 * 250 ms, minus the little time the test itself took.
	 */
	private void assertWaits(long... millis) {
		assertEquals("waits: " + _waits, millis.length, _waits.size());
		for (int i = 0; i < millis.length; i++) {
			long actual = _waits.get(i).toMillis();
			assertTrue("wait " + i + " of " + _waits + " is about " + millis[i] + " ms",
				actual > millis[i] - 200 && actual < millis[i] + 250);
		}
	}

	private BraveSearchEngine brave(RecordingServer api) {
		BraveSearchEngine.Config config = TypedConfiguration.newConfigItem(BraveSearchEngine.Config.class);
		set(config, BraveSearchEngine.Config.API_KEY, apiKey());
		set(config, BraveSearchEngine.Config.BASE_URL, api.origin() + "/res/v1/web/search");
		return new BraveSearchEngine(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config, _waits::add);
	}

	private TavilySearchEngine tavily(RecordingServer api) {
		TavilySearchEngine.Config config = TypedConfiguration.newConfigItem(TavilySearchEngine.Config.class);
		set(config, TavilySearchEngine.Config.API_KEY, apiKey());
		set(config, TavilySearchEngine.Config.BASE_URL, api.origin());
		return new TavilySearchEngine(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config, _waits::add);
	}

	private static LiteralSecretSource.Config apiKey() {
		LiteralSecretSource.Config result = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
		result.setValue("test-key");
		return result;
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	private static Reply json(String body) {
		return new Reply(200, "OK", List.of("Content-Type: application/json"), body);
	}

	private static Reply rateLimited(List<String> headers) {
		return new Reply(429, "Too Many Requests", headers, "{\"error\":\"rate limited\"}");
	}

	/** Answers the requests with {@code replies} in order, and with the last one after that. */
	private static Function<String, Reply> sequence(Reply... replies) {
		AtomicInteger next = new AtomicInteger();
		return target -> replies[Math.min(next.getAndIncrement(), replies.length - 1)];
	}
}
