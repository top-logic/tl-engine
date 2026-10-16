/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.sync;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.index.config.IndexedType;
import com.top_logic.tl.ai.index.config.IndexedTypes;
import com.top_logic.tl.ai.index.embed.Embedder;
import com.top_logic.tl.ai.index.embed.EmbeddingException;
import com.top_logic.tl.ai.index.lucene.ChunkDocument;
import com.top_logic.tl.ai.index.lucene.IndexGeneration;
import com.top_logic.tl.ai.index.lucene.IndexSchema;
import com.top_logic.tl.ai.index.lucene.StoredChunk;
import com.top_logic.tl.ai.index.lucene.Vectors;
import com.top_logic.tl.ai.index.text.Chunker;

/**
 * Brings a batch of objects into a generation: resolves each, computes its chunks, embeds what has
 * no vector yet, and replaces the object's chunks.
 *
 * <p>
 * <b>A vector is computed once.</b> Before anything is embedded, the chunks the object already has
 * - in the generation written to, and in an older generation of the same model - are read back; a
 * chunk whose embedded text has the same hash keeps its vector. So a change of one field of an
 * object with a long text embeds that field only, and a new generation after a change of the
 * analysis or the keywords embeds nothing.
 * </p>
 *
 * <p>
 * All chunks of the batch that need a vector go to the embedder in one call. Must run in an
 * interaction that may see every object - the worker's system interaction.
 * </p>
 */
public final class ObjectIndexer {

	private final KnowledgeBase _kb;

	private final IndexedTypes _types;

	private final Chunker _chunker;

	private final int _maxTextChars;

	private final Embedder _embedder;

	/**
	 * Creates an {@link ObjectIndexer}.
	 *
	 * @param embedder
	 *        The embedder of the generation, or <code>null</code> for an index of words only.
	 */
	public ObjectIndexer(KnowledgeBase kb, IndexedTypes types, Chunker chunker, int maxTextChars, Embedder embedder) {
		_kb = kb;
		_types = types;
		_chunker = chunker;
		_maxTextChars = maxTextChars;
		_embedder = embedder;
	}

	/**
	 * Indexes the given objects into {@code target}.
	 *
	 * @param target
	 *        The generation to write.
	 * @param reuse
	 *        A generation of the same embedding to take unchanged vectors from, or <code>null</code>.
	 * @param keys
	 *        The current keys of the objects; a deleted or not indexed object is removed.
	 * @param revision
	 *        The revision the objects are read at.
	 * @return How many chunks were embedded.
	 */
	public int index(IndexGeneration target, IndexGeneration reuse, Collection<ObjectKey> keys, long revision)
			throws IOException, EmbeddingException {
		Map<String, IndexedType.Content> contents = new LinkedHashMap<>();
		List<String> removed = new ArrayList<>();
		for (ObjectKey key : keys) {
			KnowledgeItem item = _kb.resolveObjectKey(key);
			String id = key.asString();
			if (item == null || !item.isAlive()) {
				removed.add(id);
				continue;
			}
			TLObject object = item.getWrapper();
			IndexedType type = _types.forObject(object);
			if (type == null) {
				removed.add(id);
				continue;
			}
			IndexedType.Content content;
			try {
				content = type.content(object, _chunker, _maxTextChars);
			} catch (RuntimeException ex) {
				// One object must not stop the index: it keeps its chunks until it changes again.
				Logger.warn("Cannot index " + id + ", its chunks stay as they were: " + ex.getMessage(), ex,
					ObjectIndexer.class);
				continue;
			}
			contents.put(ReadFilter.idOf(object), content);
		}
		for (String id : removed) {
			target.delete(id);
		}
		if (contents.isEmpty()) {
			return 0;
		}

		Map<String, float[]> known = new HashMap<>();
		if (_embedder != null) {
			known(target, contents.keySet(), known);
			if (reuse != null) {
				known(reuse, contents.keySet(), known);
			}
		}

		Map<IndexedType.PendingChunk, String> hashes = new HashMap<>();
		Map<String, String> missing = new LinkedHashMap<>();
		if (_embedder != null) {
			for (IndexedType.Content content : contents.values()) {
				for (IndexedType.PendingChunk chunk : content.chunks()) {
					String hash = hash(chunk.embeddingText());
					hashes.put(chunk, hash);
					if (!known.containsKey(hash)) {
						missing.putIfAbsent(hash, chunk.embeddingText());
					}
				}
			}
			if (!missing.isEmpty()) {
				List<float[]> vectors = _embedder.embedPassages(new ArrayList<>(missing.values()));
				int i = 0;
				for (String hash : missing.keySet()) {
					known.put(hash, vectors.get(i++));
				}
			}
		}

		String model = _embedder == null ? null : _embedder.modelId();
		for (Map.Entry<String, IndexedType.Content> entry : contents.entrySet()) {
			String id = entry.getKey();
			IndexedType.Content content = entry.getValue();
			List<ChunkDocument> chunks = new ArrayList<>(content.chunks().size());
			for (IndexedType.PendingChunk chunk : content.chunks()) {
				String hash = hashes.get(chunk);
				float[] vector = hash == null ? null : known.get(hash);
				chunks.add(new ChunkDocument(id, chunk.part(), chunk.chunk().locator(), content.types(),
					content.scopes(), content.keywords(), revision, chunk.chunk().text(), vector == null ? null : hash,
					vector == null ? null : model, vector));
			}
			target.replace(id, chunks);
		}
		return missing.size();
	}

	/**
	 * Adds the vectors the given generation holds for the given objects, by the hash of their
	 * embedded text.
	 */
	private static void known(IndexGeneration generation, Collection<String> objects, Map<String, float[]> result)
			throws IOException {
		generation.search(searcher -> {
			for (String object : objects) {
				List<StoredChunk> stored = IndexGeneration.chunksOf(searcher, object);
				List<StoredChunk> withVector = new ArrayList<>();
				for (StoredChunk chunk : stored) {
					if (chunk.textHash() != null && !result.containsKey(chunk.textHash())) {
						withVector.add(chunk);
					}
				}
				if (withVector.isEmpty()) {
					continue;
				}
				int[] docs = withVector.stream().mapToInt(StoredChunk::doc).toArray();
				Map<Integer, float[]> vectors = Vectors.read(searcher.getIndexReader(), IndexSchema.VECTOR, docs);
				for (StoredChunk chunk : withVector) {
					float[] vector = vectors.get(chunk.doc());
					if (vector != null) {
						result.put(chunk.textHash(), vector);
					}
				}
			}
			return null;
		});
	}

	/**
	 * The hash of an embedded text.
	 */
	static String hash(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest, 0, 16);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
