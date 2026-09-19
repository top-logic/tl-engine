/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.TidResolver;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

/**
 * The one place a vector-index hit becomes something a caller gets to see (issue #295).
 *
 * <p>
 * The index is written by a background daemon under system rights, so its <em>contents</em> are
 * unrestricted by construction: every object of a configured type is embedded, whoever may read it.
 * The guarantee this class implements is therefore the one that is enforceable over such an index -
 * <b>at query time</b>: a hit whose source object the principal may not read is dropped before the
 * hit reaches anybody, together with its text.
 * </p>
 *
 * <h2>What it enforces</h2>
 *
 * <ul>
 * <li><b>Fail closed.</b> A hit is kept only when its source object could be named, resolved and read.
 * A hit whose payload carries no source identifier, or whose identifier names nothing, is
 * <em>dropped</em>, not passed through: an unattributable chunk is one no read check can be made
 * about.</li>
 * <li><b>The same chokepoint as everything else.</b> The resolution goes through
 * {@link TidResolver#resolve(String, com.top_logic.tl.ai.core.security.ModelSecurity)}, which is
 * itself the read check - so "does not exist" and "not yours" are one answer here too, and no second
 * read policy is written in this module.</li>
 * <li><b>Over-fetch, then truncate.</b> Filtering happens over a larger candidate pool than the caller
 * asked for and the readable hits are cut to the caller's limit afterwards, so a principal with narrow
 * access still gets a full page rather than the two survivors of the top ten. A multiplier is a
 * heuristic and not a guarantee, which is why {@link Hits#candidatesExhausted()} says when the pool ran
 * out.</li>
 * </ul>
 *
 * <h2>What it does not claim</h2>
 *
 * <p>
 * <b>The embedding is derived from unfiltered text.</b> Dropping a hit keeps the chunk's text out of
 * the answer; it does not undo that the vector was computed from content the principal may not read,
 * and a caller who can observe scores can still learn that <em>something</em> similar exists. Not
 * indexing unreadable content in the first place is the stronger guarantee and a different change with
 * a very different cost - see {@code doc/security/agent-security-architecture.md} 3.4.3.
 * </p>
 *
 * @see ReadFilter
 */
public final class VectorReadFilter {

	/**
	 * The payload field the indexer writes the source object's identifier into.
	 *
	 * @see VectorIndex#addDocumentOnly(com.top_logic.knowledge.objects.KnowledgeObject, String, String)
	 */
	public static final String SOURCE_ID_FIELD = "objectId";

	/**
	 * The metadata key carrying the same identifier, for a store whose metadata the query side can read
	 * without parsing the payload.
	 *
	 * <p>
	 * Written since issue #295. It is read first and the payload field is the fallback, so an index
	 * built before that still filters rather than going dark.
	 * </p>
	 */
	public static final String SOURCE_ID_KEY = "TopLogic-id";

	/**
	 * How many candidates to ask the store for per result the caller wants.
	 *
	 * @see #overfetch(int)
	 */
	public static final int DEFAULT_OVERFETCH_FACTOR = 5;

	private VectorReadFilter() {
		// Static utility.
	}

	/**
	 * One search hit the principal may read, with the object it came from.
	 *
	 * @param match
	 *        The store's hit, carrying the score and the indexed text.
	 * @param source
	 *        The model object the chunk was indexed from. Never <code>null</code>: a hit without a
	 *        readable source does not become a {@link Hit}.
	 */
	public record Hit(EmbeddingMatch<TextSegment> match, TLObject source) {

		/** Creates a {@link Hit}. */
		public Hit {
			Objects.requireNonNull(match, "A hit needs the store's match.");
			Objects.requireNonNull(source, "A hit without a readable source object must be dropped,"
				+ " not constructed.");
		}
	}

	/**
	 * The readable hits of one search.
	 *
	 * @param hits
	 *        The hits, best score first, at most as many as the caller asked for.
	 * @param candidatesExhausted
	 *        Whether the filter ran out of candidates before it could fill the caller's limit. True
	 *        means "there may be more, ask for more"; it does <em>not</em> mean that something was
	 *        withheld, since a short answer is also what a small index gives.
	 */
	public record Hits(List<Hit> hits, boolean candidatesExhausted) {

		/** Creates a {@link Hits}. */
		public Hits {
			hits = List.copyOf(hits);
		}
	}

	/**
	 * How many candidates to request from the store for a caller that wants {@code limit} results.
	 *
	 * @param limit
	 *        The number of readable hits the caller asked for.
	 * @return The candidate pool size, never smaller than {@code limit}.
	 */
	public static int overfetch(int limit) {
		if (limit <= 0) {
			return 0;
		}
		int candidates = limit * DEFAULT_OVERFETCH_FACTOR;
		// Guard the multiplication rather than trust the caller's limit: an absurd limit must not wrap
		// into a negative pool size and turn the filter into a no-op.
		return candidates < limit ? Integer.MAX_VALUE : candidates;
	}

	/**
	 * The hits of the given candidates the principal may read.
	 *
	 * @param candidates
	 *        What the store returned, in its own order. May be <code>null</code>.
	 * @param filter
	 *        The read filter of the principal being served. Mandatory: a search that cannot name whose
	 *        rights apply would have to disclose everything or nothing.
	 * @param limit
	 *        How many readable hits the caller asked for.
	 * @return The readable hits, never <code>null</code>.
	 */
	public static Hits readable(List<EmbeddingMatch<TextSegment>> candidates, ReadFilter filter, int limit) {
		if (filter == null) {
			throw new IllegalStateException("What a vector-index search may disclose is decided against"
				+ " the principal it serves, and this call names none. Pass the ReadFilter of the run.");
		}
		if (candidates == null || candidates.isEmpty() || limit <= 0) {
			return new Hits(List.of(), false);
		}

		List<Hit> result = new ArrayList<>(Math.min(limit, candidates.size()));
		int seen = 0;
		for (EmbeddingMatch<TextSegment> candidate : candidates) {
			seen++;
			TLObject source = readableSource(candidate, filter);
			if (source == null) {
				continue;
			}
			result.add(new Hit(candidate, source));
			if (result.size() == limit) {
				break;
			}
		}

		// Exhausted means: every candidate the store offered was looked at and the limit is still not
		// full. If the store had more to give, the caller's next page is where they are.
		boolean exhausted = result.size() < limit && seen == candidates.size();
		return new Hits(result, exhausted);
	}

	/**
	 * The object the given hit was indexed from, if the principal may read it.
	 *
	 * @return The source object, or <code>null</code> when the hit names none, names one that no longer
	 *         exists, or names one the principal may not read - three cases that are deliberately one
	 *         answer, because telling them apart would disclose which.
	 */
	private static TLObject readableSource(EmbeddingMatch<TextSegment> match, ReadFilter filter) {
		String id = sourceId(match.embedded());
		if (id == null) {
			return null;
		}
		try {
			return TidResolver.resolve(id, filter.security());
		} catch (RuntimeException ex) {
			// Malformed, gone, or not readable. TidResolver spells the last two the same way on
			// purpose, and a chunk nobody can attribute is no safer than one that is refused.
			return null;
		}
	}

	/**
	 * The source object's identifier recorded with the given chunk, or <code>null</code> if it carries
	 * none.
	 *
	 * <p>
	 * Reads the {@link #SOURCE_ID_KEY} metadata entry first and falls back to the
	 * {@link #SOURCE_ID_FIELD} field of the indexed payload, which is where the indexer put it before
	 * issue #295. Both are the same value written by the same code; neither is a security decision,
	 * which is taken by the caller of this method.
	 * </p>
	 *
	 * @param segment
	 *        The indexed chunk. May be <code>null</code>.
	 */
	public static String sourceId(TextSegment segment) {
		if (segment == null) {
			return null;
		}
		if (segment.metadata() != null) {
			String fromMetadata = segment.metadata().getString(SOURCE_ID_KEY);
			if (fromMetadata != null && !fromMetadata.isEmpty()) {
				return fromMetadata;
			}
		}
		return sourceIdOfPayload(segment.text());
	}

	private static String sourceIdOfPayload(String text) {
		if (text == null || text.isEmpty()) {
			return null;
		}
		Object parsed;
		try {
			parsed = new JSONParser().parse(text);
		} catch (Exception ex) {
			// A chunk that is not the indexer's payload names no object, which is all this asked.
			return null;
		}
		if (!(parsed instanceof JSONObject payload)) {
			return null;
		}
		Object id = payload.get(SOURCE_ID_FIELD);
		return id == null || id.toString().isEmpty() ? null : id.toString();
	}
}
