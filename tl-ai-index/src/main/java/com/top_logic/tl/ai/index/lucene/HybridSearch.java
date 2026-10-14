/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.QueryBuilder;

/**
 * One query over words and meaning: BM25 on the German and the English field, k nearest neighbours
 * on the vectors, both under the same filter, fused by {@link Rrf} and collapsed to objects.
 *
 * <p>
 * <b>Bounded.</b> Each side reads at most {@link Request#depth()} chunks, whatever the size of the
 * index. A caller that needs more asks again with a larger depth.
 * </p>
 *
 * <p>
 * <b>Vector recall.</b> The vectors are quantized to 7 bits, and HNSW is approximate. The meaning
 * side therefore asks the graph for {@link Request#overfetch()} times the depth and orders those
 * candidates by the exact dot product of the raw float vectors, which the quantized format keeps.
 * With 4 times, recall@10 against exact search was 1.0 in the scout's measurement.
 * </p>
 */
public final class HybridSearch {

	/** The largest number of candidates the vector graph is asked for. */
	public static final int MAX_KNN = 10_000;

	/** The tie breaker of the disjunction over the two language fields. */
	private static final float LANGUAGE_TIE_BREAKER = 0.1f;

	private HybridSearch() {
		// Static utility.
	}

	/**
	 * What to search.
	 *
	 * @param words
	 *        The word query without filter, or <code>null</code> for no word side; see
	 *        {@link HybridSearch#words(Analyzer, String)}.
	 * @param vector
	 *        The query vector, or <code>null</code> for no meaning side.
	 * @param filter
	 *        Which chunks may be found.
	 * @param depth
	 *        How many chunks each side reads at most.
	 * @param overfetch
	 *        How many times the depth the vector graph is asked for, before the exact re-score.
	 * @param fusion
	 *        How the two rankings are fused.
	 */
	public record Request(Query words, float[] vector, IndexFilter filter, int depth, int overfetch, Rrf fusion) {
		// A plain carrier.
	}

	/**
	 * One object found, with the best chunk that found it.
	 *
	 * @param object
	 *        The object identifier.
	 * @param score
	 *        The fused score.
	 * @param wordRank
	 *        The object's rank on the word side, from 1; 0 when words did not find it.
	 * @param meaningRank
	 *        The object's rank on the meaning side, from 1; 0 when meaning did not find it.
	 * @param cosine
	 *        The cosine of the object's best chunk to the query vector; {@link Float#NaN} when meaning
	 *        did not find it.
	 * @param chunk
	 *        The chunk that ranked the object best.
	 */
	public record ObjectHit(String object, double score, int wordRank, int meaningRank, float cosine,
			StoredChunk chunk) {
		// A plain carrier.
	}

	/**
	 * The fused objects of a search.
	 *
	 * @param hits
	 *        Every object either side found, best first.
	 * @param chunksRead
	 *        How many chunks both sides read together; bounded by the request, not by the index.
	 * @param exhausted
	 *        Whether both sides found fewer chunks than they were allowed to read, so a deeper search
	 *        would find nothing more.
	 */
	public record Result(List<ObjectHit> hits, int chunksRead, boolean exhausted) {
		// A plain carrier.
	}

	/**
	 * The word query for a text the user wrote: its terms on both language fields, as one
	 * disjunction, so a word that matches in both fields does not count twice.
	 *
	 * @return The query, or <code>null</code> when the text has no searchable word (only stop words).
	 */
	public static Query words(Analyzer analyzer, String text) {
		if (text == null || text.isBlank()) {
			return null;
		}
		QueryBuilder builder = new QueryBuilder(analyzer);
		List<Query> perField = new ArrayList<>(IndexSchema.WORD_FIELDS.length);
		for (String field : IndexSchema.WORD_FIELDS) {
			Query query = builder.createBooleanQuery(field, text, Occur.SHOULD);
			if (query != null) {
				perField.add(query);
			}
		}
		return perField.isEmpty() ? null : new DisjunctionMaxQuery(perField, LANGUAGE_TIE_BREAKER);
	}

	/**
	 * The word query for "objects like these texts": the most characteristic terms of the texts, by
	 * Lucene's {@link MoreLikeThis}, on both language fields.
	 *
	 * @return The query, or <code>null</code> when the texts have no characteristic term.
	 */
	public static Query like(IndexSearcher searcher, Analyzer analyzer, List<String> texts) throws IOException {
		if (texts.isEmpty()) {
			return null;
		}
		MoreLikeThis mlt = new MoreLikeThis(searcher.getIndexReader());
		mlt.setAnalyzer(analyzer);
		mlt.setMinTermFreq(1);
		mlt.setMinDocFreq(1);
		mlt.setMaxQueryTerms(25);
		List<Query> perField = new ArrayList<>(IndexSchema.WORD_FIELDS.length);
		for (String field : IndexSchema.WORD_FIELDS) {
			mlt.setFieldNames(new String[] { field });
			StringReader[] readers = new StringReader[texts.size()];
			for (int i = 0; i < readers.length; i++) {
				readers[i] = new StringReader(texts.get(i));
			}
			Query query = mlt.like(field, readers);
			if (query instanceof BooleanQuery bool && bool.clauses().isEmpty()) {
				continue;
			}
			perField.add(query);
		}
		return perField.isEmpty() ? null : new DisjunctionMaxQuery(perField, LANGUAGE_TIE_BREAKER);
	}

	/**
	 * Runs the search.
	 */
	public static Result search(IndexSearcher searcher, Request request) throws IOException {
		Query filter = request.filter().toQuery();
		StoredFields stored = searcher.storedFields();
		int depth = request.depth();

		List<ScoreDoc> wordChunks = List.of();
		if (request.words() != null) {
			Query query = request.words();
			if (filter != null) {
				query = new BooleanQuery.Builder()
					.add(query, Occur.MUST)
					.add(filter, Occur.FILTER)
					.build();
			}
			wordChunks = List.of(searcher.search(query, depth).scoreDocs);
		}

		List<ScoreDoc> meaningChunks = List.of();
		int knnRead = 0;
		if (request.vector() != null) {
			float[] vector = Vectors.normalized(request.vector());
			int k = Math.min(MAX_KNN, Math.max(depth, depth * request.overfetch()));
			TopDocs candidates = searcher.search(new KnnFloatVectorQuery(IndexSchema.VECTOR, vector, k, filter), k);
			knnRead = candidates.scoreDocs.length;
			meaningChunks = rescore(searcher, vector, candidates.scoreDocs, depth);
		}

		Map<String, Side> words = collapse(stored, wordChunks);
		Map<String, Side> meanings = collapse(stored, meaningChunks);

		Rrf fusion = request.fusion();
		Map<String, ObjectHit> fused = new HashMap<>();
		for (Map.Entry<String, Side> entry : words.entrySet()) {
			String object = entry.getKey();
			Side word = entry.getValue();
			Side meaning = meanings.get(object);
			int meaningRank = meaning == null ? 0 : meaning.rank();
			int bestDoc = meaning != null && fusion.meaning(meaningRank) > fusion.word(word.rank()) ? meaning.doc()
				: word.doc();
			fused.put(object, new ObjectHit(object, fusion.score(word.rank(), meaningRank), word.rank(), meaningRank,
				meaning == null ? Float.NaN : meaning.score(), StoredChunk.read(bestDoc, stored.document(bestDoc))));
		}
		for (Map.Entry<String, Side> entry : meanings.entrySet()) {
			String object = entry.getKey();
			if (fused.containsKey(object)) {
				continue;
			}
			Side meaning = entry.getValue();
			fused.put(object, new ObjectHit(object, fusion.score(0, meaning.rank()), 0, meaning.rank(),
				meaning.score(), StoredChunk.read(meaning.doc(), stored.document(meaning.doc()))));
		}
		List<ObjectHit> hits = new ArrayList<>(fused.values());
		hits.sort(Comparator.comparingDouble(ObjectHit::score).reversed().thenComparing(ObjectHit::object));
		boolean exhausted = wordChunks.size() < depth
			&& (request.vector() == null || knnRead < Math.min(MAX_KNN, Math.max(depth, depth * request.overfetch())));
		return new Result(hits, wordChunks.size() + knnRead, exhausted);
	}

	/**
	 * The candidates ordered by the exact dot product of their raw vectors with the query, the best
	 * {@code depth} of them, with that product as score.
	 */
	private static List<ScoreDoc> rescore(IndexSearcher searcher, float[] vector, ScoreDoc[] candidates, int depth)
			throws IOException {
		int[] docs = new int[candidates.length];
		for (int i = 0; i < docs.length; i++) {
			docs[i] = candidates[i].doc;
		}
		Map<Integer, float[]> raw = Vectors.read(searcher.getIndexReader(), IndexSchema.VECTOR, docs);
		List<ScoreDoc> exact = new ArrayList<>(candidates.length);
		for (ScoreDoc candidate : candidates) {
			float[] stored = raw.get(candidate.doc);
			if (stored != null) {
				exact.add(new ScoreDoc(candidate.doc, Vectors.dot(stored, vector)));
			}
		}
		exact.sort(Comparator.comparingDouble((ScoreDoc doc) -> doc.score).reversed()
			.thenComparingInt(doc -> doc.doc));
		return exact.size() > depth ? exact.subList(0, depth) : exact;
	}

	/** An object's place on one side: its rank, its best chunk and that chunk's score. */
	private record Side(int rank, int doc, float score) {
		// A plain carrier.
	}

	/**
	 * The objects of a ranked chunk list, each with the rank of its first chunk among the objects.
	 */
	private static Map<String, Side> collapse(StoredFields stored, List<ScoreDoc> chunks) throws IOException {
		Map<String, Side> result = new LinkedHashMap<>();
		Set<String> objectField = Set.of(IndexSchema.OBJECT);
		for (ScoreDoc chunk : chunks) {
			String object = stored.document(chunk.doc, objectField).get(IndexSchema.OBJECT);
			if (!result.containsKey(object)) {
				result.put(object, new Side(result.size() + 1, chunk.doc, chunk.score));
			}
		}
		return result;
	}

}
