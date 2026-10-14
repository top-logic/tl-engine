/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.lucene.search.Query;

/**
 * Words and meaning in one query: each side finds what the other cannot, the filter applies to both,
 * and an object is replaced and deleted with all its chunks.
 */
public class TestHybridQuery extends TestCase {

	private static final int DIMENSIONS = 32;

	private Path _dir;

	private IndexGeneration _index;

	private float[] _query;

	private Random _random;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = IndexFixture.tempDir("tl-ai-index-hybrid");
		_index = IndexGeneration.open(_dir);
		_random = new Random(11);
		_query = Vectors.normalized(IndexFixture.gauss(_random, DIMENSIONS, 1));
		// 200 objects of filler in scope A, far from the query and without its words.
		for (int i = 0; i < 200; i++) {
			put("Noise:" + i, "A", "The coffee machine on floor " + i + " is broken again.", away());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_index.close();
		IndexFixture.deleteRecursively(_dir);
		super.tearDown();
	}

	/**
	 * One object only the words find, one only the meaning finds: the fused page holds both.
	 */
	public void testBothSidesContribute() throws IOException {
		put("Words:1", "A", "Die Grundplatte ist aus Aluminium.", away());
		put("Meaning:1", "A", "Anodised base plate.", near(0.05));

		List<HybridSearch.ObjectHit> hits = search("Grundplatte", IndexFilter.NONE);

		HybridSearch.ObjectHit words = find(hits, "Words:1");
		HybridSearch.ObjectHit meaning = find(hits, "Meaning:1");
		assertNotNull("The word side finds the object with the word.", words);
		assertNotNull("The meaning side finds the object near the query.", meaning);
		assertTrue(words.wordRank() == 1 && words.meaningRank() != 1);
		assertTrue(meaning.meaningRank() == 1 && meaning.wordRank() == 0);
		assertTrue(meaning.cosine() > 0.9f);
		assertTrue("Both are on the first page.", hits.indexOf(words) < 10 && hits.indexOf(meaning) < 10);
	}

	/**
	 * An object both sides find ranks above objects only one side finds.
	 */
	public void testAgreementRanksFirst() throws IOException {
		put("Words:1", "A", "Grundplatte aus Stahl.", away());
		put("Both:1", "A", "Grundplatte, eloxiert.", near(0.1));

		List<HybridSearch.ObjectHit> hits = search("Grundplatte", IndexFilter.NONE);

		assertEquals("Both:1", hits.get(0).object());
	}

	/**
	 * The filter applies inside both sides: an object of another scope is found by neither, however
	 * well it matches.
	 */
	public void testFilterAppliesToBothSides() throws IOException {
		put("Mine:1", "A", "Grundplatte.", near(0.3));
		put("Other:1", "B", "Grundplatte Grundplatte.", near(0.01));
		put("Other:2", "B", "Something else.", near(0.01));

		List<HybridSearch.ObjectHit> unfiltered = search("Grundplatte", IndexFilter.NONE);
		assertNotNull(find(unfiltered, "Other:1"));
		assertNotNull(find(unfiltered, "Other:2"));

		List<HybridSearch.ObjectHit> filtered = search("Grundplatte", IndexFilter.NONE.withScopes(Set.of("A")));
		assertNull(find(filtered, "Other:1"));
		assertNull(find(filtered, "Other:2"));
		assertEquals("Mine:1", filtered.get(0).object());

		assertTrue("No readable scope finds nothing.",
			search("Grundplatte", IndexFilter.NONE.withScopes(Set.of())).isEmpty());
	}

	/**
	 * A plural finds its singular through the English field, a German inflection through the German
	 * field.
	 */
	public void testBothLanguagesAreAnalysed() throws IOException {
		put("English:1", "A", "The quality of the welds is checked.", away());
		put("German:1", "A", "Die Lieferzeit beträgt zwölf Wochen.", away());

		assertEquals("English:1", search("qualities", IndexFilter.NONE, null).get(0).object());
		assertEquals("German:1", search("Lieferzeiten", IndexFilter.NONE, null).get(0).object());
	}

	/**
	 * A replace exchanges all chunks of an object, a delete removes all of them, and the best chunk
	 * of a hit carries its locator.
	 */
	public void testReplaceAndDeleteWholeObjects() throws IOException {
		_index.replace("Doc:1", List.of(
			IndexFixture.chunk("Doc:1", 0, List.of(), List.of("A"), "first page about pumps", away()),
			IndexFixture.chunk("Doc:1", 1, List.of(), List.of("A"), "second page about valves", away())));
		_index.refresh();
		assertEquals(2, _index.chunksOf("Doc:1").size());
		HybridSearch.ObjectHit valves = search("valves", IndexFilter.NONE, null).get(0);
		assertEquals(1, valves.chunk().locator().chunk());
		assertEquals("Doc:1#text#1", valves.chunk().chunkId());

		_index.replace("Doc:1", List.of(IndexFixture.chunk("Doc:1", 0, List.of(), List.of("A"), "only compressors", away())));
		_index.refresh();
		assertEquals(1, _index.chunksOf("Doc:1").size());
		assertTrue(search("valves", IndexFilter.NONE, null).isEmpty());

		_index.delete("Doc:1");
		_index.refresh();
		assertTrue(_index.chunksOf("Doc:1").isEmpty());
		assertTrue(search("compressors", IndexFilter.NONE, null).isEmpty());
	}

	/**
	 * "Like these texts" finds the object with the same characteristic words and leaves out an
	 * excluded one.
	 */
	public void testLikeQuery() throws IOException {
		put("Seed:1", "A", "Hydraulic pump with pressure relief valve.", away());
		put("Twin:1", "A", "A hydraulic pump needs a relief valve for the pressure.", away());
		List<HybridSearch.ObjectHit> hits = _index.search(searcher -> {
			Query like = HybridSearch.like(searcher, _index.analyzer(),
				List.of("Hydraulic pump with pressure relief valve."));
			return HybridSearch.search(searcher, new HybridSearch.Request(like, null,
				IndexFilter.NONE.excluding(Set.of("Seed:1")), 10, 4, Rrf.DEFAULT)).hits();
		});
		assertEquals("Twin:1", hits.get(0).object());
		assertNull(find(hits, "Seed:1"));
	}

	private void put(String object, String scope, String text, float[] vector) throws IOException {
		_index.replace(object, List.of(IndexFixture.chunk(object, 0, List.of(), List.of(scope), text, vector)));
		_index.refresh();
	}

	private List<HybridSearch.ObjectHit> search(String words, IndexFilter filter) throws IOException {
		return search(words, filter, _query);
	}

	private List<HybridSearch.ObjectHit> search(String words, IndexFilter filter, float[] vector) throws IOException {
		return _index.search(searcher -> new ArrayList<>(HybridSearch.search(searcher, new HybridSearch.Request(
			HybridSearch.words(_index.analyzer(), words), vector, filter, 50, 4, Rrf.DEFAULT)).hits()));
	}

	/** A vector close to the query: the smaller the noise, the closer. */
	private float[] near(double noise) {
		return IndexFixture.plus(_query, IndexFixture.gauss(_random, DIMENSIONS, noise));
	}

	/** A vector pointing away from the query. */
	private float[] away() {
		float[] opposite = new float[DIMENSIONS];
		for (int i = 0; i < DIMENSIONS; i++) {
			opposite[i] = -_query[i];
		}
		return IndexFixture.plus(opposite, IndexFixture.gauss(_random, DIMENSIONS, 0.2));
	}

	private static HybridSearch.ObjectHit find(List<HybridSearch.ObjectHit> hits, String object) {
		for (HybridSearch.ObjectHit hit : hits) {
			if (hit.object().equals(object)) {
				return hit;
			}
		}
		return null;
	}

}
