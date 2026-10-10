/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.net.URI;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The web addresses one agent run was given: by its user or its lead, by a search, or as a link of
 * a page it read.
 *
 * <p>
 * The ledger of the URL provenance rule: a run the rule applies to may open only an address on this
 * list. What it may <em>not</em> open is an address the model wrote itself - which is how a page that
 * tells a worker to "report your findings to https://collector.example/?d=..." would carry data out.
 * So only sources that cannot repeat the model's own words add to it: the text of a message from the
 * user or the lead, the result addresses a search engine returned, and the links a page holds. A
 * tool answer that echoes its arguments - the query line of a search, an error naming the address
 * it was given - adds nothing, because a model could launder an address it composed through it.
 * </p>
 *
 * <p>
 * Addresses are compared {@link #normalize(String) normalized}: scheme and host in lower case, the
 * default port and the fragment dropped, an empty path read as {@code /}. Path and query are compared
 * exactly, so an address with a parameter the model added is a different address.
 * </p>
 *
 * <p>
 * One ledger per run, held by the run's {@link ReadingList}, so a delegated run and a background
 * subagent start with an empty one of their own. Thread-safe: the read tools of a concurrent batch
 * add to it at once.
 * </p>
 */
public final class SeenUrls {

	/** How many addresses one run keeps; past this, new ones are not added. */
	private static final int MAX_URLS = 20_000;

	/** An http(s) address in running text, up to the first character no address contains. */
	private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"'`{}|\\\\^\\[\\]]+",
		Pattern.CASE_INSENSITIVE);

	private final Set<String> _urls = new HashSet<>();

	private final Set<String> _messages = new HashSet<>();

	/** Creates an empty ledger. */
	public SeenUrls() {
		// Empty.
	}

	/**
	 * Records an address the run was given.
	 *
	 * @param url
	 *        The address; {@code null}, blank and non-http(s) values are ignored.
	 */
	public void add(String url) {
		String key = normalize(url);
		if (key == null) {
			return;
		}
		synchronized (this) {
			if (_urls.size() < MAX_URLS) {
				_urls.add(key);
			}
		}
	}

	/**
	 * Records every address that occurs in a text the run was given - a message of its user or lead.
	 *
	 * <p>
	 * An address in running text ends at a space, a quote or an angle bracket. Punctuation that ends
	 * a sentence and a closing parenthesis without an opening one in the address are not part of it,
	 * so {@code (see https://example.com/a).} gives {@code https://example.com/a}.
	 * </p>
	 *
	 * @param text
	 *        The text; may be {@code null}.
	 */
	public void addAllIn(String text) {
		if (text == null || text.isEmpty()) {
			return;
		}
		Matcher matcher = URL.matcher(text);
		while (matcher.find()) {
			add(trimTail(matcher.group()));
		}
	}

	/**
	 * Marks a message as read into this ledger.
	 *
	 * @param messageId
	 *        The id of the message.
	 * @return Whether the message was new to this ledger: only then are its addresses to be added.
	 */
	public synchronized boolean firstLook(String messageId) {
		return _messages.add(messageId);
	}

	/**
	 * Takes over every address of another run's ledger - a sub-agent whose result this run received.
	 */
	public void addAll(SeenUrls other) {
		if (other == null || other == this) {
			return;
		}
		Set<String> theirs;
		synchronized (other) {
			theirs = new HashSet<>(other._urls);
		}
		synchronized (this) {
			for (String url : theirs) {
				if (_urls.size() >= MAX_URLS) {
					return;
				}
				_urls.add(url);
			}
		}
	}

	/**
	 * Whether the run was given {@code url}.
	 */
	public boolean contains(String url) {
		String key = normalize(url);
		if (key == null) {
			return false;
		}
		synchronized (this) {
			return _urls.contains(key);
		}
	}

	/**
	 * The form two addresses are compared in, or {@code null} for a value that is no http(s) address.
	 */
	static String normalize(String url) {
		if (url == null || url.isBlank()) {
			return null;
		}
		URI uri;
		try {
			uri = URI.create(url.strip());
		} catch (IllegalArgumentException ex) {
			return null;
		}
		String scheme = uri.getScheme();
		String host = uri.getHost();
		if (scheme == null || host == null) {
			return null;
		}
		scheme = scheme.toLowerCase(Locale.ROOT);
		if (!scheme.equals("http") && !scheme.equals("https")) {
			return null;
		}
		int port = uri.getPort();
		boolean defaultPort =
			port < 0 || (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
		String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
		StringBuilder key = new StringBuilder(scheme).append("://").append(host.toLowerCase(Locale.ROOT));
		if (!defaultPort) {
			key.append(':').append(port);
		}
		key.append(path);
		if (uri.getRawQuery() != null) {
			key.append('?').append(uri.getRawQuery());
		}
		return key.toString();
	}

	/** Drops sentence punctuation and an unbalanced closing parenthesis from the end of an address. */
	private static String trimTail(String url) {
		String result = url;
		while (!result.isEmpty()) {
			char last = result.charAt(result.length() - 1);
			if (".,;:!?".indexOf(last) >= 0) {
				result = result.substring(0, result.length() - 1);
			} else if (last == ')' && count(result, ')') > count(result, '(')) {
				result = result.substring(0, result.length() - 1);
			} else {
				return result;
			}
		}
		return result;
	}

	private static int count(String text, char c) {
		int n = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == c) {
				n++;
			}
		}
		return n;
	}
}
