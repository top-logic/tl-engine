/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.tl.ai.tool.api.ReadSource;

/**
 * What one agent run read: the sources its answer may point at.
 *
 * <p>
 * The framework writes this list, not the model. Every tool that reads a source registers it - an
 * object a read tool showed, a page {@code webFetch} or the browser read, an application's own
 * record - and {@link CitationValidator} keeps a link in the answer as a footnote only when it
 * points at an entry of this list. A source no tool read does not count, however plausible its
 * identifier looks.
 * </p>
 *
 * <p>
 * <b>One list per run, merged on delivery.</b> A sub-agent's run gets a list of its own, and its
 * entries join the delegating run's list when its result is handed over - when {@code delegate}
 * returns, when {@code wait_for_subagent} receives it, or when it is injected at the start of a
 * later turn. Not by a pointer to the parent's list: a detached sub-agent outlives the run that
 * spawned it, and the answer that uses its result is checked against the list of the run that
 * received it.
 * </p>
 *
 * <p>
 * Thread-safe: the read tools of one turn run in parallel and register into the same list.
 * </p>
 */
public final class ReadingList {

	/**
	 * How much of a source's read text is kept for the run.
	 *
	 * <p>
	 * The text is what the popover's excerpt and a quoted passage are checked against. A tool's own
	 * answer is capped well below this, so nothing the model could have seen is cut off here.
	 * </p>
	 */
	private static final int MAX_TEXT = 200_000;

	/**
	 * How many sources one run keeps.
	 *
	 * <p>
	 * A query that shows the model ten thousand objects registers each of them, and an answer cites
	 * a handful. Past this many the list stops growing rather than holding the run's whole result in
	 * memory; what was read first stays citable.
	 * </p>
	 */
	private static final int MAX_SOURCES = 5_000;

	private final Map<String, ReadSource> _sources = new LinkedHashMap<>();

	/** Reference -> key of the entry it names: the entry's own key, or an alias of it. */
	private final Map<String, String> _references = new LinkedHashMap<>();

	private final SeenUrls _seenUrls = new SeenUrls();

	/** Creates an empty reading list. */
	public ReadingList() {
		// Empty.
	}

	/**
	 * Registers a source this run read.
	 *
	 * <p>
	 * A source read twice is one entry. The later reading replaces the earlier one only where it
	 * knows more - a text where there was none, an archived copy where there was none - so a page
	 * read once by the browser and once by {@code webFetch} keeps whatever either of them found.
	 * </p>
	 *
	 * @param source
	 *        What was read. A source without a reference is ignored: nothing can point at it.
	 * @param aliases
	 *        Further references the answer may write for the same source - the URL the model asked
	 *        for, when a redirect led to another one.
	 */
	public void add(ReadSource source, String... aliases) {
		if (source == null) {
			return;
		}
		String key = normalize(source.url().isEmpty() ? source.id() : source.url());
		if (key.isEmpty()) {
			return;
		}
		ReadSource bounded = source.text().length() > MAX_TEXT
			? source.withText(source.text().substring(0, MAX_TEXT))
			: source;
		synchronized (this) {
			ReadSource known = _sources.get(key);
			if (known == null && _sources.size() >= MAX_SOURCES) {
				return;
			}
			_sources.put(key, known == null ? bounded : merged(known, bounded));
			_references.put(key, key);
			_references.putIfAbsent(normalize(source.id()), key);
			for (String alias : aliases) {
				String reference = normalize(alias);
				if (!reference.isEmpty()) {
					_references.putIfAbsent(reference, key);
				}
			}
		}
	}

	private static ReadSource merged(ReadSource known, ReadSource next) {
		String title = known.title().isEmpty() ? next.title() : known.title();
		String text = next.text().isEmpty() ? known.text() : next.text();
		String archive = next.archiveUrl().isEmpty() ? known.archiveUrl() : next.archiveUrl();
		Instant readAt = next.archiveUrl().isEmpty() ? known.readAt() : next.readAt();
		return new ReadSource(known.kind(), known.id(), title, known.url(), text, archive, readAt);
	}

	/**
	 * The web addresses this run was given - the ledger of the URL provenance rule.
	 *
	 * <p>
	 * Kept with the reading list because it has the same scope: one per run, shared by every copy of
	 * the run's context, a fresh one for a delegation, and taken over with the sources when a
	 * sub-agent's result is delivered.
	 * </p>
	 */
	public SeenUrls seenUrls() {
		return _seenUrls;
	}

	/**
	 * Takes over every source of another run's list - a sub-agent whose result this run received.
	 *
	 * @param other
	 *        The list to merge; <code>null</code> and this list itself change nothing.
	 */
	public void addAll(ReadingList other) {
		if (other == null || other == this) {
			return;
		}
		Map<String, ReadSource> sources;
		Map<String, String> references;
		synchronized (other) {
			sources = new LinkedHashMap<>(other._sources);
			references = new LinkedHashMap<>(other._references);
		}
		_seenUrls.addAll(other._seenUrls);
		synchronized (this) {
			for (Map.Entry<String, ReadSource> entry : sources.entrySet()) {
				ReadSource known = _sources.get(entry.getKey());
				if (known == null && _sources.size() >= MAX_SOURCES) {
					continue;
				}
				_sources.put(entry.getKey(), known == null ? entry.getValue() : merged(known, entry.getValue()));
			}
			for (Map.Entry<String, String> entry : references.entrySet()) {
				_references.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * The source a reference in the answer names, or <code>null</code> when this run read none.
	 *
	 * @param reference
	 *        An identifier or a URL, as the answer wrote it.
	 */
	public synchronized ReadSource find(String reference) {
		String key = _references.get(normalize(reference));
		return key == null ? null : _sources.get(key);
	}

	/** Every source of this run, in the order they were first read. */
	public synchronized List<ReadSource> sources() {
		return List.copyOf(new ArrayList<>(_sources.values()));
	}

	/** Whether this run read nothing it could cite. */
	public synchronized boolean isEmpty() {
		return _sources.isEmpty();
	}

	/**
	 * The form a reference is compared in.
	 *
	 * <p>
	 * An identifier is compared as written. A URL is compared without what does not name another
	 * page: the fragment, a default port, the case of scheme and host, and a trailing slash of the
	 * path - the model writes {@code https://Example.org/a/} for the page it read as
	 * {@code https://example.org/a}.
	 * </p>
	 */
	static String normalize(String reference) {
		if (reference == null) {
			return "";
		}
		String trimmed = reference.trim();
		if (!isWebAddress(trimmed)) {
			return trimmed;
		}
		try {
			URI uri = URI.create(trimmed);
			String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
			String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
			int port = uri.getPort();
			boolean defaultPort = port < 0 || ("http".equals(scheme) && port == 80)
				|| ("https".equals(scheme) && port == 443);
			String path = uri.getRawPath() == null ? "" : uri.getRawPath();
			while (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
			return scheme + "://" + host + (defaultPort ? "" : ":" + port) + path + query;
		} catch (IllegalArgumentException ex) {
			return trimmed;
		}
	}

	/** Whether {@code reference} is an absolute http(s) address. */
	public static boolean isWebAddress(String reference) {
		if (reference == null) {
			return false;
		}
		String lower = reference.trim().toLowerCase(Locale.ROOT);
		return lower.startsWith("http://") || lower.startsWith("https://");
	}
}
