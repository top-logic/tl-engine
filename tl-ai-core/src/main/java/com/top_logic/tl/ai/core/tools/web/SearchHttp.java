/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.top_logic.basic.Logger;

/**
 * Sends the requests of a {@link WebSearchEngine} and waits out a rate limit.
 *
 * <p>
 * A search API answers {@code 429 Too Many Requests} when its plan's rate is exceeded - Brave's free
 * plan allows one request per second, which a few research workers searching at once exceed at
 * once. The answer is not an error of the search but a request to come back later, so the request
 * is sent again, up to {@link #MAX_RETRIES} times. How long to wait is taken from the answer: the
 * {@code Retry-After} header (seconds or a date), else the reset time of the exhausted window in the
 * {@code X-RateLimit-Reset} header Brave sends, else a doubling backoff from one second with a little
 * jitter, so that workers that were refused together do not come back together. A temporary server
 * failure ({@code 502}, {@code 503}, {@code 504}) is retried the same way.
 * </p>
 *
 * <p>
 * <b>Requests are paced once the rate is known.</b> Every refused worker is told the same reset time,
 * so workers that wait exactly that long come back together and are refused together again (seen
 * against Brave with four workers). The {@code X-RateLimit-Policy} header Brave sends with every answer
 * names the plan's rate ({@code 1;w=1}: one request per second); from then on this engine gives every
 * request - retry or new - a slot of its own, a little more than one interval after the one before.
 * Only windows up to a minute long set the pace; a monthly quota is not spread over the month.
 * </p>
 *
 * <p>
 * The wait is bounded: an answer that asks for more than {@link #MAX_WAIT} - an exhausted monthly
 * quota asks for days - is not waited for, because the agent run waits with it. The search then
 * fails with a sentence that says so. So does a search whose slot lies further ahead than that. A
 * search that is not sent - refused for its slot, or interrupted while it waits - takes no slot from
 * the searches after it.
 * </p>
 */
final class SearchHttp {

	/** How often a refused request is sent again. */
	static final int MAX_RETRIES = 3;

	/** The longest wait this class accepts before one retry. */
	static final Duration MAX_WAIT = Duration.ofSeconds(20);

	/** The wait before the first retry when the answer names none. */
	static final Duration FIRST_BACKOFF = Duration.ofSeconds(1);

	/** The longest rate-limit window that sets the pace of requests. */
	private static final long MAX_PACING_WINDOW_SECONDS = 60;

	/** Waits between two attempts. A test replaces it, so that it does not wait. */
	interface Sleeper {

		/** The sleeper on the real clock. */
		Sleeper SYSTEM = duration -> Thread.sleep(duration.toMillis());

		/** Waits for {@code duration}. */
		void sleep(Duration duration) throws InterruptedException;
	}

	private final String _engine;

	private final HttpClient _client;

	private final Sleeper _sleeper;

	private final Clock _clock;

	/** The interval between two requests, learned from an answer; zero while no rate is known. */
	private Duration _spacing = Duration.ZERO;

	/** The earliest time the next request may be sent. */
	private Instant _nextSlot = Instant.MIN;

	/**
	 * Creates a {@link SearchHttp}.
	 *
	 * @param engine
	 *        The name of the engine, for the messages.
	 * @param client
	 *        The client that sends the requests.
	 * @param sleeper
	 *        Waits between two attempts.
	 * @param clock
	 *        The clock a {@code Retry-After} date is compared with.
	 */
	SearchHttp(String engine, HttpClient client, Sleeper sleeper, Clock clock) {
		_engine = engine;
		_client = client;
		_sleeper = sleeper;
		_clock = clock;
	}

	/**
	 * Sends {@code request}, again after a rate limit or a temporary server failure.
	 *
	 * @return The first answer that is neither; it may still be an error, which the caller reports.
	 * @throws IOException
	 *         When the request cannot be sent, or is still refused after {@link #MAX_RETRIES} retries,
	 *         or the answer asks for a wait longer than {@link #MAX_WAIT}.
	 */
	HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
		Duration wait = Duration.ZERO;
		for (int attempt = 0;; attempt++) {
			Instant now = _clock.instant();
			Instant slot = reserveSlot(now, wait);
			Duration untilSlot = Duration.between(now, slot);
			if (untilSlot.compareTo(MAX_WAIT) > 0) {
				throw new IOException(_engine + " search is rate-limited: the searches already waiting take the"
					+ " next " + untilSlot.toSeconds() + " seconds. Search less often, or wait before you search"
					+ " again.");
			}
			if (!untilSlot.isZero() && !untilSlot.isNegative()) {
				try {
					_sleeper.sleep(untilSlot);
				} catch (InterruptedException ex) {
					releaseSlot(slot);
					throw ex;
				}
			}
			HttpResponse<String> response = _client.send(request, BodyHandlers.ofString());
			learnPace(response);
			int status = response.statusCode();
			if (!retryable(status)) {
				return response;
			}
			wait = waitBefore(response, attempt);
			if (wait.compareTo(MAX_WAIT) > 0) {
				throw new IOException(_engine + " search is " + reason(status) + " (HTTP " + status
					+ ") and asks to wait " + wait.toSeconds() + " seconds before the next request."
					+ " Do not search again in this run.");
			}
			if (attempt >= MAX_RETRIES) {
				throw new IOException(_engine + " search is " + reason(status) + " (HTTP " + status
					+ ") and still was after " + MAX_RETRIES + " retries. Wait before you search again.");
			}
			Logger.info(_engine + " search answered HTTP " + status + "; retrying in at least " + wait.toMillis()
				+ " ms (retry " + (attempt + 1) + " of " + MAX_RETRIES + ").", SearchHttp.class);
		}
	}

	/**
	 * Books the next free slot that is at least {@code wait} after {@code now}.
	 *
	 * <p>
	 * A slot further ahead than {@link #MAX_WAIT} is returned without being booked: the caller does not
	 * send its request, and a slot booked for it would push every later search out by one interval -
	 * with workers that retry after the refusal, until no search fits any more.
	 * </p>
	 *
	 * @return The slot; the caller sends its request then.
	 */
	private Instant reserveSlot(Instant now, Duration wait) {
		synchronized (this) {
			Instant earliest = now.plus(wait);
			Instant slot = earliest.isAfter(_nextSlot) ? earliest : _nextSlot;
			if (Duration.between(now, slot).compareTo(MAX_WAIT) <= 0) {
				_nextSlot = slot.plus(_spacing);
			}
			return slot;
		}
	}

	/**
	 * Gives back {@code slot}, whose request is not sent, when no later slot was booked after it.
	 *
	 * <p>
	 * A slot with later ones behind it stays booked: the searches behind it keep their slots, and the
	 * gap is one interval in which nothing is sent.
	 * </p>
	 */
	private void releaseSlot(Instant slot) {
		synchronized (this) {
			if (_nextSlot.equals(slot.plus(_spacing))) {
				_nextSlot = slot;
			}
		}
	}

	/**
	 * Takes the plan's rate from an {@code X-RateLimit-Policy} header such as
	 * {@code "1;w=1, 2000;w=2678400"}: the slowest pace any window of up to a minute demands.
	 */
	private void learnPace(HttpResponse<?> response) {
		Optional<String> policy = response.headers().firstValue("x-ratelimit-policy");
		if (policy.isEmpty()) {
			return;
		}
		Duration spacing = Duration.ZERO;
		for (String window : policy.get().split(",")) {
			String[] parts = window.split(";");
			try {
				long limit = Long.parseLong(parts[0].strip());
				long seconds = -1;
				for (int i = 1; i < parts.length; i++) {
					String parameter = parts[i].strip();
					if (parameter.startsWith("w=")) {
						seconds = Long.parseLong(parameter.substring(2).strip());
					}
				}
				if (limit > 0 && seconds > 0 && seconds <= MAX_PACING_WINDOW_SECONDS) {
					// A tenth more than the plan's interval: a slot exactly on the boundary of the
					// provider's fixed window is refused as often as not.
					Duration interval = Duration.ofSeconds(seconds).multipliedBy(11).dividedBy(10 * limit);
					if (interval.compareTo(spacing) > 0) {
						spacing = interval;
					}
				}
			} catch (NumberFormatException ex) {
				// A window this class cannot read sets no pace.
			}
		}
		synchronized (this) {
			_spacing = spacing;
			// The request just answered was booked before its pace was known.
			Instant next = _clock.instant().plus(spacing);
			if (next.isAfter(_nextSlot)) {
				_nextSlot = next;
			}
		}
	}

	private static boolean retryable(int status) {
		return status == 429 || status == 502 || status == 503 || status == 504;
	}

	private static String reason(int status) {
		return status == 429 ? "rate-limited" : "temporarily unavailable";
	}

	/** How long to wait before the retry that follows {@code attempt}. Package-visible for testing. */
	Duration waitBefore(HttpResponse<?> response, int attempt) {
		Optional<Duration> asked = retryAfter(response.headers().firstValue("retry-after"));
		if (asked.isEmpty()) {
			asked = rateLimitReset(response.headers().firstValue("x-ratelimit-reset"),
				response.headers().firstValue("x-ratelimit-remaining"));
		}
		long jitter = ThreadLocalRandom.current().nextLong(250);
		if (asked.isPresent()) {
			return asked.get().plusMillis(jitter);
		}
		return FIRST_BACKOFF.multipliedBy(1L << Math.min(attempt, 10)).plusMillis(jitter);
	}

	/** The wait a {@code Retry-After} header asks for: seconds, or an HTTP date. */
	private Optional<Duration> retryAfter(Optional<String> header) {
		if (header.isEmpty() || header.get().isBlank()) {
			return Optional.empty();
		}
		String value = header.get().strip();
		try {
			return Optional.of(Duration.ofSeconds(Math.max(0, Long.parseLong(value))));
		} catch (NumberFormatException notSeconds) {
			try {
				ZonedDateTime at = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
				Duration until = Duration.between(_clock.instant(), at.toInstant());
				return Optional.of(until.isNegative() ? Duration.ZERO : until);
			} catch (DateTimeParseException notADate) {
				return Optional.empty();
			}
		}
	}

	/**
	 * The wait Brave's rate-limit headers ask for.
	 *
	 * <p>
	 * Both headers list one value per window, shortest window first ({@code "1, 1419200"}: one
	 * second, the rest of the month). The wait is the reset time of the longest window that has
	 * nothing left; when the remaining counts are missing, it is that of the first window.
	 * </p>
	 */
	private static Optional<Duration> rateLimitReset(Optional<String> resets, Optional<String> remaining) {
		if (resets.isEmpty()) {
			return Optional.empty();
		}
		long[] reset = numbers(resets.get());
		if (reset.length == 0) {
			return Optional.empty();
		}
		long[] left = remaining.map(SearchHttp::numbers).orElse(new long[0]);
		long wait = -1;
		for (int i = 0; i < reset.length && i < left.length; i++) {
			if (left[i] <= 0) {
				wait = Math.max(wait, reset[i]);
			}
		}
		return Optional.of(Duration.ofSeconds(Math.max(0, wait < 0 ? reset[0] : wait)));
	}

	private static long[] numbers(String header) {
		String[] parts = header.split(",");
		long[] values = new long[parts.length];
		try {
			for (int i = 0; i < parts.length; i++) {
				values[i] = Long.parseLong(parts[i].strip());
			}
			return values;
		} catch (NumberFormatException ex) {
			return new long[0];
		}
	}
}
