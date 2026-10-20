/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import com.top_logic.ai.vector.index.VectorReadFilter;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

/**
 * End-to-end test that a vector-index hit reaches nobody who may not read the object it came from
 * (issue #295).
 *
 * <p>
 * The index is the read surface with the weakest starting position: a background daemon fills it
 * under system rights, so its contents are unrestricted by construction and every guarantee has to be
 * made at query time. What is asserted here is the guarantee that is enforceable over such an index -
 * a hit whose source the acting persona may not read is <b>dropped</b>, with its text - against the
 * fixture's real accounts and the application's real {@code <security-config>}.
 * </p>
 *
 * <p>
 * Every case is a <b>pair</b>, for the reason {@link SecDemoReadFilterTest} states: an empty result on
 * its own is also what a broken filter returns, so each hit that must disappear for one persona is
 * asserted to survive for another.
 * </p>
 *
 * <p>
 * The hits are built here rather than embedded and retrieved, because what is under test is the read
 * check and not the store: embedding needs a Mistral key and a running index, which is why
 * {@code VectorIndexIntegrationTest} is behind {@code -Dtl.ai.vector.e2e=true} and this is not. The
 * chunks carry exactly what the indexer writes - the {@code TopLogic-id} metadata entry and the
 * {@code objectId} payload field - so the identity path under test is the shipped one.
 * </p>
 *
 * @see VectorReadFilter
 */
public class SecDemoVectorIndexTest extends SecDemoPersonaTest {

	/** A body of text a chunk might carry, recognisable in an assertion. */
	private static final String SECRET_TEXT = "confidential-chunk-text";

	/**
	 * A partner reads the hit for the project she partners on, and not the hit for the other one.
	 *
	 * <p>
	 * Two hits of the same type, one persona, two different answers - which is per-object filtering and
	 * the criterion the whole change exists for.
	 * </p>
	 */
	public void testAHitIsFilteredPerObject() {
		List<EmbeddingMatch<TextSegment>> candidates =
			List.of(hitFor(project(ALPHA)), hitFor(project(BETA)));

		List<TLObject> forAnna = sources(candidates, ANNA, 10);
		assertEquals("Anna partners on exactly one of the two projects.", 1, forAnna.size());
		assertSame(project(ALPHA), forAnna.get(0));

		List<TLObject> forCara = sources(candidates, CARA, 10);
		assertEquals("Cara partners on the other one, so the same two hits answer differently.",
			1, forCara.size());
		assertSame(project(BETA), forCara.get(0));
	}

	/**
	 * An account with no Read grant on the type gets nothing, and the chunk's text goes with the hit.
	 *
	 * <p>
	 * The text is the disclosure that matters here: it is a serialization of the object's attributes
	 * taken at indexing time, so a hit that survived with its text would leak the object twice over.
	 * </p>
	 */
	public void testAnUnreadableHitTakesItsTextWithIt() {
		List<EmbeddingMatch<TextSegment>> candidates = List.of(hitFor(project(ALPHA)));

		VectorReadFilter.Hits forBen =
			VectorReadFilter.readable(candidates, readFilterFor(BEN), 10);
		assertTrue("A ticket reader holds no Read grant on pm:Project at all.", forBen.hits().isEmpty());

		VectorReadFilter.Hits forAnna =
			VectorReadFilter.readable(candidates, readFilterFor(ANNA), 10);
		assertEquals("The pair makes the drop attributable to the grant.", 1, forAnna.hits().size());
		assertTrue("...and the text is what the drop withheld.",
			forAnna.hits().get(0).match().embedded().text().contains(SECRET_TEXT));
	}

	/**
	 * The persona who scopes nothing reads no hit, though she holds the tools.
	 *
	 * <p>
	 * {@code Dora} is the fixture's "write tools and nothing to write" account. Her empty answer is what
	 * makes the Anna pair above a statement about rights and not about the query.
	 * </p>
	 */
	public void testAnAccountThatScopesNothingReadsNoHit() {
		List<EmbeddingMatch<TextSegment>> candidates =
			List.of(hitFor(project(ALPHA)), hitFor(project(BETA)));

		assertTrue("Dora scopes no project.", sources(candidates, DORA, 10).isEmpty());
	}

	/**
	 * A hit nobody can attribute to an object is dropped, not passed through.
	 *
	 * <p>
	 * Fail closed: a chunk that names no source, or names one that no longer exists, is one no read
	 * check can be made about, and "we could not check" must not read as "you may see it". The third
	 * case - a source the persona may not read - is deliberately the same answer, so that the three
	 * cannot be told apart from outside.
	 * </p>
	 */
	public void testAnUnattributableHitIsDropped() {
		EmbeddingMatch<TextSegment> noSource = hit(TextSegment.from("{\"text\": \"" + SECRET_TEXT + "\"}"));
		EmbeddingMatch<TextSegment> notAPayload = hit(TextSegment.from(SECRET_TEXT));
		// A well-formed identifier of the right type naming nothing - the shape a chunk of a deleted
		// object keeps until the daemon catches up with the deletion.
		EmbeddingMatch<TextSegment> goneSource =
			hitFor(tidOf(project(ALPHA)).replaceAll(":.*$", ":99999999"));

		List<EmbeddingMatch<TextSegment>> candidates = new ArrayList<>();
		candidates.add(noSource);
		candidates.add(notAPayload);
		candidates.add(goneSource);
		candidates.add(hitFor(project(ALPHA)));

		List<TLObject> forAnna = sources(candidates, ANNA, 10);
		assertEquals("Only the one attributable, readable hit survives.", 1, forAnna.size());
		assertSame(project(ALPHA), forAnna.get(0));
	}

	/**
	 * The source is read from the payload too, so an index written before issue #295 still filters.
	 *
	 * <p>
	 * The metadata entry is the newer of the two records and is read first; the {@code objectId} field
	 * of the payload is what the indexer has always written. A chunk carrying only the old one must
	 * filter rather than be dropped wholesale, or switching the filter on would silently empty every
	 * existing index.
	 * </p>
	 */
	public void testTheSourceIsAlsoReadFromTheIndexedPayload() {
		EmbeddingMatch<TextSegment> legacy = hit(TextSegment.from(payloadOf(tidOf(project(ALPHA)))));

		assertEquals("The payload names the object, so the hit is attributable.",
			List.of(project(ALPHA)), sources(List.of(legacy), ANNA, 10));
		assertTrue("...and it is filtered, not waved through.",
			sources(List.of(legacy), BEN, 10).isEmpty());
	}

	/**
	 * The caller's limit is what bounds the answer, and it is applied after the filtering.
	 *
	 * <p>
	 * Truncating before the check is the mistake this is here to prevent: a page cut from the
	 * unfiltered candidates comes back short - or empty - for a narrowly scoped account, with no
	 * explanation and no way to ask for the rest. Over-fetching and then cutting is why
	 * {@link VectorReadFilter#overfetch(int)} exists.
	 * </p>
	 */
	public void testTheLimitIsAppliedAfterTheFilter() {
		List<EmbeddingMatch<TextSegment>> candidates = new ArrayList<>();
		// Four hits Anna may not read, ahead of the one she may: cutting to one before the check
		// would hand her nothing at all.
		for (int i = 0; i < 4; i++) {
			candidates.add(hitFor(project(BETA)));
		}
		candidates.add(hitFor(project(ALPHA)));

		VectorReadFilter.Hits oneWanted = VectorReadFilter.readable(candidates, readFilterFor(ANNA), 1);
		assertEquals("The readable hit is found behind the unreadable ones.", 1, oneWanted.hits().size());
		assertSame(project(ALPHA), oneWanted.hits().get(0).source());
		assertFalse("The limit was filled, so nothing says the pool ran out.",
			oneWanted.candidatesExhausted());

		VectorReadFilter.Hits tenWanted = VectorReadFilter.readable(candidates, readFilterFor(ANNA), 10);
		assertEquals("Asking for ten gets the one that exists.", 1, tenWanted.hits().size());
		assertTrue("...and says that the candidates, not the index, were the bound.",
			tenWanted.candidatesExhausted());

		assertTrue("The pool asked of the store is a multiple of what the caller wants.",
			VectorReadFilter.overfetch(10) > 10);
	}

	/**
	 * A search that cannot name its principal does not happen.
	 *
	 * <p>
	 * The invariant, as a runtime check rather than as a convention: there is no form of
	 * {@code VectorIndex.searchVectorIndex} without a {@link ReadFilter}, and the filter itself refuses
	 * a <code>null</code> one rather than defaulting to a permissive answer.
	 * </p>
	 */
	public void testASearchWithoutAPrincipalIsRefused() {
		try {
			VectorReadFilter.readable(List.of(hitFor(project(ALPHA))), null, 10);
			fail("A search with no principal must be refused, not answered.");
		} catch (IllegalStateException expected) {
			assertTrue("The refusal must say what is missing: " + expected.getMessage(),
				expected.getMessage().contains("ReadFilter"));
		}
	}

	/** The objects behind the readable hits of the given candidates, for the given persona. */
	private List<TLObject> sources(List<EmbeddingMatch<TextSegment>> candidates, String login, int limit) {
		List<TLObject> result = new ArrayList<>();
		for (VectorReadFilter.Hit hit : VectorReadFilter.readable(candidates, readFilterFor(login), limit)
			.hits()) {
			result.add(hit.source());
		}
		return result;
	}

	/** The read filter of a run acting as the given persona. */
	private ReadFilter readFilterFor(String login) {
		return ReadFilter.forSecurity(securityFor(login).modelSecurity());
	}

	/** A hit for the given object, carrying what the indexer records about it. */
	private static EmbeddingMatch<TextSegment> hitFor(TLObject object) {
		return hitFor(tidOf(object));
	}

	/** A hit for the given identifier, whether or not it names anything. */
	private static EmbeddingMatch<TextSegment> hitFor(String tid) {
		return hit(TextSegment.from(payloadOf(tid),
			Metadata.from(VectorReadFilter.SOURCE_ID_KEY, tid)));
	}

	private static EmbeddingMatch<TextSegment> hit(TextSegment segment) {
		return new EmbeddingMatch<>(Double.valueOf(0.9), "embedding-" + segment.text().hashCode(), null,
			segment);
	}

	/** The shape the indexer writes: a JSON object whose {@code objectId} names the source. */
	private static String payloadOf(String tid) {
		return "{\"" + VectorReadFilter.SOURCE_ID_FIELD + "\": \"" + tid + "\", \"content\": \""
			+ SECRET_TEXT + "\"}";
	}

	private static String tidOf(TLObject object) {
		return object.tId().asString();
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoVectorIndexTest.class);
	}
}
