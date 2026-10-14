/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 * The Lucene directory of one generation of the index: its writer, its searchers and its commit
 * data.
 *
 * <p>
 * All chunks of one object are replaced in one {@link IndexWriter#updateDocuments} call, so a reader
 * never sees half of an object's new chunks. A replace is idempotent, which makes replaying a range
 * of the history after a crash safe.
 * </p>
 *
 * <p>
 * A commit always carries the commit data the caller gives - for the index service, the watermark
 * of the history it has applied. The writer does not commit on close: work after the last commit is
 * replayed from the history at the next start rather than committed without a watermark.
 * </p>
 */
public final class IndexGeneration implements Closeable {

	/** Commit data: the last revision whose changes are fully in the index. */
	public static final String WATERMARK = "watermark";

	/** Commit data: the fingerprint of the configuration the generation was built with. */
	public static final String FINGERPRINT = "fingerprint";

	/** Commit data: the embedding model of the generation's vectors; empty in word mode. */
	public static final String EMBEDDING_MODEL = "embeddingModel";

	/** Commit data: whether the generation has reached the history once ({@code "true"}). */
	public static final String COMPLETE = "complete";

	/** At most this many chunks of one object are read back by {@link #chunksOf(String)}. */
	public static final int MAX_CHUNKS_PER_OBJECT = 10_000;

	private final Path _path;

	private final FSDirectory _directory;

	private final IndexWriter _writer;

	private final SearcherManager _searchers;

	private final Analyzer _analyzer;

	private IndexGeneration(Path path, FSDirectory directory, IndexWriter writer, Analyzer analyzer)
			throws IOException {
		_path = path;
		_directory = directory;
		_writer = writer;
		_analyzer = analyzer;
		_searchers = new SearcherManager(writer, null);
	}

	/**
	 * Opens the generation in the given directory, creating it when it does not exist.
	 */
	public static IndexGeneration open(Path path) throws IOException {
		Files.createDirectories(path);
		FSDirectory directory = FSDirectory.open(path);
		try {
			Analyzer analyzer = IndexSchema.analyzer();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(OpenMode.CREATE_OR_APPEND);
			config.setCodec(new IndexCodec());
			config.setCommitOnClose(false);
			config.setRAMBufferSizeMB(32);
			IndexWriter writer = new IndexWriter(directory, config);
			return new IndexGeneration(path, directory, writer, analyzer);
		} catch (IOException | RuntimeException ex) {
			directory.close();
			throw ex;
		}
	}

	/**
	 * The directory of this generation.
	 */
	public Path path() {
		return _path;
	}

	/**
	 * The analyzer the word fields were written with, which a query must use too.
	 */
	public Analyzer analyzer() {
		return _analyzer;
	}

	/**
	 * The commit data of the last commit, or of the pending one when it was set since.
	 */
	public Map<String, String> commitData() {
		Map<String, String> result = new HashMap<>();
		Iterable<Map.Entry<String, String>> data = _writer.getLiveCommitData();
		if (data != null) {
			for (Map.Entry<String, String> entry : data) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	/**
	 * Replaces all chunks of the given object by the given ones; no chunks deletes the object.
	 */
	public void replace(String object, List<ChunkDocument> chunks) throws IOException {
		Term term = new Term(IndexSchema.OBJECT, object);
		if (chunks.isEmpty()) {
			_writer.deleteDocuments(term);
			return;
		}
		List<Document> docs = new ArrayList<>(chunks.size());
		for (ChunkDocument chunk : chunks) {
			if (!object.equals(chunk.object())) {
				throw new IllegalArgumentException("Chunk '" + chunk.chunkId() + "' does not belong to " + object + ".");
			}
			docs.add(chunk.toDocument());
		}
		_writer.updateDocuments(term, docs);
	}

	/**
	 * Removes all chunks of the given object.
	 */
	public void delete(String object) throws IOException {
		_writer.deleteDocuments(new Term(IndexSchema.OBJECT, object));
	}

	/**
	 * Removes every chunk.
	 */
	public void deleteAll() throws IOException {
		_writer.deleteAll();
	}

	/**
	 * Commits everything written so far together with the given commit data, and makes it visible to
	 * searches.
	 */
	public void commit(Map<String, String> data) throws IOException {
		_writer.setLiveCommitData(new HashMap<>(data).entrySet());
		_writer.commit();
		_searchers.maybeRefreshBlocking();
	}

	/**
	 * Makes what was written visible to searches, without a commit.
	 */
	public void refresh() throws IOException {
		_searchers.maybeRefreshBlocking();
	}

	/**
	 * Runs the given search on the newest visible state of the index.
	 */
	public <T> T search(SearchFunction<T> search) throws IOException {
		IndexSearcher searcher = _searchers.acquire();
		try {
			return search.apply(searcher);
		} finally {
			_searchers.release(searcher);
		}
	}

	/**
	 * The number of chunks in the index.
	 */
	public int chunkCount() throws IOException {
		return search(searcher -> searcher.getIndexReader().numDocs());
	}

	/**
	 * The stored chunks of the given object, in chunk order.
	 */
	public List<StoredChunk> chunksOf(String object) throws IOException {
		return search(searcher -> chunksOf(searcher, object));
	}

	/**
	 * The stored chunks of the given object in the given searcher, in chunk order.
	 */
	public static List<StoredChunk> chunksOf(IndexSearcher searcher, String object) throws IOException {
		TopDocs docs = searcher.search(new TermQuery(new Term(IndexSchema.OBJECT, object)),
			MAX_CHUNKS_PER_OBJECT, new Sort(SortField.FIELD_DOC));
		StoredFields fields = searcher.storedFields();
		List<StoredChunk> result = new ArrayList<>(docs.scoreDocs.length);
		for (ScoreDoc hit : docs.scoreDocs) {
			result.add(StoredChunk.read(hit.doc, fields.document(hit.doc)));
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		try {
			_searchers.close();
			_writer.close();
		} finally {
			_directory.close();
		}
	}

	/**
	 * A search on an {@link IndexSearcher}.
	 */
	@FunctionalInterface
	public interface SearchFunction<T> {

		/**
		 * Searches.
		 */
		T apply(IndexSearcher searcher) throws IOException;
	}

}
