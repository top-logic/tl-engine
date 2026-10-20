/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.IntPredicate;

import junit.framework.TestCase;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 * The meaning side finds what an exact search over the chunks that pass the filter finds.
 *
 * <p>
 * 5,000 synthetic vectors of 64 dimensions in 100 tight clusters, 1,000 scopes and two types, fixed
 * seeds. The ground truth of each query is the exact dot product over the documents the filter lets
 * pass. The vectors are quantized to 7 bits and the graph is approximate, so recall depends on the
 * over-fetch and the exact re-score of {@link HybridSearch}.
 * </p>
 */
public class TestIndexRecall extends TestCase {

	private static final int DOCS = 5_000;

	private static final int DIMENSIONS = 64;

	private static final int CLUSTERS = 100;

	private static final int SCOPES = 1_000;

	private static final int QUERIES = 50;

	private static final int TOP = 10;

	private Path _dir;

	private float[][] _vectors;

	private int[] _scopeOf;

	private float[][] _queries;

	private IndexGeneration _index;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = IndexFixture.tempDir("tl-ai-index-recall");
		Random random = new Random(42);
		float[][] centers = new float[CLUSTERS][];
		for (int c = 0; c < CLUSTERS; c++) {
			centers[c] = Vectors.normalized(IndexFixture.gauss(random, DIMENSIONS, 1));
		}
		_vectors = new float[DOCS][];
		_scopeOf = new int[DOCS];
		_index = IndexGeneration.open(_dir);
		for (int i = 0; i < DOCS; i++) {
			_vectors[i] = IndexFixture.plus(centers[random.nextInt(CLUSTERS)], IndexFixture.gauss(random, DIMENSIONS, 0.11));
			_scopeOf[i] = random.nextInt(SCOPES);
			_index.replace(object(i), List.of(IndexFixture.chunk(object(i), 0, List.of(type(i)),
				List.of(scope(_scopeOf[i])), "chunk " + i, _vectors[i])));
		}
		_index.commit(Map.of(IndexGeneration.WATERMARK, "4711", IndexGeneration.EMBEDDING_MODEL, "synthetic-64"));
		Random queries = new Random(7);
		_queries = new float[QUERIES][];
		for (int q = 0; q < QUERIES; q++) {
			_queries[q] = IndexFixture.plus(centers[queries.nextInt(CLUSTERS)],
				IndexFixture.gauss(queries, DIMENSIONS, 0.11));
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (_index != null) {
			_index.close();
		}
		IndexFixture.deleteRecursively(_dir);
		super.tearDown();
	}

	/** Without a filter. */
	public void testNoFilter() throws IOException {
		assertRecall(IndexFilter.NONE, i -> true);
	}

	/** A type filter that passes about half of the chunks. */
	public void testTypeFilter() throws IOException {
		assertRecall(new IndexFilter(Set.of("Statement"), null, Map.of(), Set.of()), i -> i % 2 == 0);
	}

	/** 50 of 1,000 scopes: about 5 % of the chunks. */
	public void testFivePercentScopes() throws IOException {
		Set<String> scopes = new HashSet<>();
		for (int s = 0; s < 50; s++) {
			scopes.add(scope(s));
		}
		assertRecall(IndexFilter.NONE.withScopes(scopes), i -> _scopeOf[i] < 50);
	}

	/** One scope of 1,000: about 0.1 % of the chunks, fewer than a page. */
	public void testOneScope() throws IOException {
		assertRecall(IndexFilter.NONE.withScopes(Set.of(scope(3))), i -> _scopeOf[i] == 3);
	}

	/**
	 * The index is read without this module's codec class, and its commit data comes back: the
	 * per-field vector format is found by its name in the segment.
	 */
	public void testPlainReaderReadsTheIndex() throws IOException {
		_index.close();
		_index = null;
		try (FSDirectory directory = FSDirectory.open(_dir); DirectoryReader reader = DirectoryReader.open(directory)) {
			assertEquals(DOCS, reader.numDocs());
			Map<String, String> data = reader.getIndexCommit().getUserData();
			assertEquals("4711", data.get(IndexGeneration.WATERMARK));
			assertEquals("synthetic-64", data.get(IndexGeneration.EMBEDDING_MODEL));
			TopDocs hits = new IndexSearcher(reader).search(new KnnFloatVectorQuery(IndexSchema.VECTOR, _queries[0], 5), 5);
			assertEquals(5, hits.scoreDocs.length);
		}
		_index = IndexGeneration.open(_dir);
		assertEquals("The commit data survives a reopen of the writer.", "4711",
			_index.commitData().get(IndexGeneration.WATERMARK));
		assertEquals(DOCS, _index.chunkCount());
	}

	private void assertRecall(IndexFilter filter, IntPredicate passes) throws IOException {
		double sum = 0;
		for (float[] query : _queries) {
			List<String> found = _index.search(searcher -> {
				HybridSearch.Result result = HybridSearch.search(searcher,
					new HybridSearch.Request(null, query, filter, TOP, 4, Rrf.DEFAULT, 0f));
				List<String> objects = new ArrayList<>();
				for (HybridSearch.ObjectHit hit : result.hits()) {
					assertTrue("A hit passes the filter.", passes.test(index(hit.object())));
					objects.add(hit.object());
				}
				return objects;
			});
			Set<String> truth = truth(query, passes);
			int hit = 0;
			for (String object : found.subList(0, Math.min(TOP, found.size()))) {
				if (truth.contains(object)) {
					hit++;
				}
			}
			sum += truth.isEmpty() ? 1 : (double) hit / truth.size();
		}
		double recall = sum / _queries.length;
		assertTrue("recall@10 was " + recall, recall >= 0.95);
	}

	private Set<String> truth(float[] query, IntPredicate passes) {
		List<Integer> pool = new ArrayList<>();
		for (int i = 0; i < DOCS; i++) {
			if (passes.test(i)) {
				pool.add(i);
			}
		}
		pool.sort((a, b) -> Float.compare(Vectors.dot(_vectors[b], query), Vectors.dot(_vectors[a], query)));
		Set<String> result = new HashSet<>();
		for (int i : pool.subList(0, Math.min(TOP, pool.size()))) {
			result.add(object(i));
		}
		return result;
	}

	private static String object(int i) {
		return "Item:" + i;
	}

	private static int index(String object) {
		return Integer.parseInt(object.substring("Item:".length()));
	}

	private static String type(int i) {
		return i % 2 == 0 ? "Statement" : "Section";
	}

	private static String scope(int s) {
		return "Project:" + s;
	}

}
