/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The documents {@link WebFetchTool} read in the last minutes, by the address the model asked for.
 *
 * <p>
 * A model reads a long document in windows, one call per window. Without this, every window would
 * fetch and extract the whole document again - for a 60-page PDF that is 60 downloads - and a page
 * that changed in between would make an offset point into a different text. Only a call that reads
 * on asks this cache; a call from the start fetches anew and replaces what is kept. The documents
 * are kept for a few minutes only, and the cache is bounded by the characters it holds, so it never
 * holds more than a few large documents.
 * </p>
 *
 * <p>
 * What is kept is what the public address answered to a request without credentials, which is the
 * same for every account. Which account may <em>see</em> it is not decided here: the tool applies
 * the acting role's {@link BrowsingPolicy} again before it answers from this cache.
 * </p>
 */
final class FetchedDocuments {

	/** How long a document is kept after it was fetched. */
	static final Duration TIME_TO_LIVE = Duration.ofMinutes(10);

	/** How many characters of text all kept documents hold together, at most. */
	static final int MAX_CHARS = 8_000_000;

	private final Clock _clock;

	private final Map<String, Entry> _entries = new LinkedHashMap<>(16, 0.75f, true);

	private long _chars;

	/** A cache on the system clock. */
	FetchedDocuments() {
		this(Clock.systemUTC());
	}

	/** A cache on the given clock. Package-visible for testing. */
	FetchedDocuments(Clock clock) {
		_clock = clock;
	}

	/**
	 * The document fetched for {@code url} in the last {@link #TIME_TO_LIVE}, or {@code null}.
	 */
	synchronized FetchedDocument get(String url) {
		dropExpired();
		Entry entry = _entries.get(url);
		return entry == null ? null : entry.document();
	}

	/**
	 * Keeps a document fetched for {@code url}. A document larger than half the cache is not kept:
	 * it would push out everything else, and is read anew instead.
	 */
	synchronized void put(String url, FetchedDocument document) {
		int size = document.text().length();
		if (size > MAX_CHARS / 2) {
			return;
		}
		Entry previous = _entries.remove(url);
		if (previous != null) {
			_chars -= previous.document().text().length();
		}
		_entries.put(url, new Entry(document, _clock.instant().plus(TIME_TO_LIVE)));
		_chars += size;
		dropExpired();
		Iterator<Entry> eldestFirst = _entries.values().iterator();
		while (_chars > MAX_CHARS && eldestFirst.hasNext()) {
			_chars -= eldestFirst.next().document().text().length();
			eldestFirst.remove();
		}
	}

	private void dropExpired() {
		Instant now = _clock.instant();
		Iterator<Entry> entries = _entries.values().iterator();
		while (entries.hasNext()) {
			Entry entry = entries.next();
			if (!entry.expires().isAfter(now)) {
				_chars -= entry.document().text().length();
				entries.remove();
			}
		}
	}

	private record Entry(FetchedDocument document, Instant expires) {
		// Value type only.
	}
}
