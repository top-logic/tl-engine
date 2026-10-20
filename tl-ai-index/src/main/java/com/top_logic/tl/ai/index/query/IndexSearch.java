/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.tl.ai.index.config.IndexedType;
import com.top_logic.tl.ai.index.config.IndexedTypes;
import com.top_logic.tl.ai.index.embed.Embedder;
import com.top_logic.tl.ai.index.embed.EmbeddingException;
import com.top_logic.tl.ai.index.lucene.HybridSearch;
import com.top_logic.tl.ai.index.lucene.IndexFilter;
import com.top_logic.tl.ai.index.lucene.IndexGeneration;
import com.top_logic.tl.ai.index.lucene.IndexSchema;
import com.top_logic.tl.ai.index.lucene.Rrf;
import com.top_logic.tl.ai.index.lucene.StoredChunk;
import com.top_logic.tl.ai.index.lucene.Vectors;
import com.top_logic.tl.ai.index.sync.IndexStatus;
import com.top_logic.tl.ai.index.sync.IndexSync;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.util.TLContext;

/**
 * Searches the index for a reader: only what the reader may read, filtered inside the index where
 * it can be and checked object by object where it must be.
 *
 * <h2>The rule</h2>
 *
 * <ol>
 * <li><b>No reader, no search.</b> A call in a system context, or without a user, is refused: the
 * engine allows every read there.</li>
 * <li><b>Scope inside the query.</b> The scopes the reader may read ({@link ReadableScopes}) are a
 * filter of both the word and the meaning side, so the best hits come from what the reader may
 * read.</li>
 * <li><b>Each hit checked.</b> In rank order, each object is resolved under the reader's rights,
 * and the reader must be allowed to read every attribute the chunk's text comes from. A hit that
 * fails is dropped with its text. The checks stop when the page is full.</li>
 * <li><b>Deeper, not wider.</b> When the page is not full and the index had more candidates, the
 * search asks again with twice the depth, up to {@code max-candidates} chunks per side and as many
 * objects checked. The answer says whether more is available, never how many hits were
 * dropped.</li>
 * </ol>
 *
 * <p>
 * Every hit on the page is registered on the run's reading list with the text the reader was shown,
 * so an answer may cite it and quote it.
 * </p>
 */
public final class IndexSearch {

	/** The fewest chunks a side reads in the first round. */
	private static final int MIN_DEPTH = 30;

	/** How many times a page the vector graph is asked for, before the exact re-score. */
	private static final int OVERFETCH = 4;

	/** The most chunks of an object a similarity search starts from. */
	private static final int SIMILAR_CHUNKS = 50;

	/**
	 * How a search turns an identifier into an object the reader may read.
	 */
	@FunctionalInterface
	interface Resolver {

		/**
		 * The object, or <code>null</code> when it does not exist or the reader may not read it.
		 */
		TLObject resolve(String id, ModelSecurity security);
	}

	/** The resolver of production: {@link TidResolver}, which is the read check. */
	static final Resolver CHECKED = (id, security) -> {
		try {
			return TidResolver.resolve(id, security);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	};

	private final IndexSync _sync;

	private final IndexedTypes _types;

	private final Embedder _embedder;

	private final ReadableScopes _scopes;

	private final KnowledgeBase _kb;

	private final int _maxCandidates;

	private final float _minSimilarity;

	private final Rrf _fusion;

	private Resolver _resolver = CHECKED;

	private volatile int _lastChunksRead;

	/**
	 * Creates an {@link IndexSearch}.
	 */
	public IndexSearch(IndexSync sync, IndexedTypes types, Embedder embedder, ReadableScopes scopes, KnowledgeBase kb,
			int maxCandidates, float minSimilarity) {
		_sync = sync;
		_types = types;
		_embedder = embedder;
		_scopes = scopes;
		_kb = kb;
		_maxCandidates = maxCandidates;
		_minSimilarity = minSimilarity;
		_fusion = Rrf.DEFAULT;
	}

	/**
	 * Test seam: how identifiers are resolved, e.g. counted.
	 */
	void setResolver(Resolver resolver) {
		_resolver = resolver;
	}

	/**
	 * Test seam: how many chunks the index read for the last page, over all rounds.
	 */
	int lastChunksRead() {
		return _lastChunksRead;
	}

	/**
	 * Searches for the request's words.
	 *
	 * @param request
	 *        What to search.
	 * @param security
	 *        The reader's rights.
	 * @return The page.
	 * @throws SearchRefusedException
	 *         When the call runs in a system context or without a user.
	 */
	public Hits search(SearchRequest request, ModelSecurity security) {
		refuseWithoutReader();
		if (request.query() == null || request.query().isBlank()) {
			return empty(request.mode(), "The query has no word to search for.");
		}
		awaitIfAsked(request);
		IndexFilter filter = filter(request, security);
		if (filter == null) {
			return empty(request.mode(), "Nothing to search: none of the requested types is indexed.");
		}
		// Before the switch lock: an embedding call can take minutes, and a switch waits for every search.
		QueryVector query = embed(request);
		try {
			return _sync.withServing(serving -> {
				if (serving == null) {
					return empty(request.mode(), "The index is being built and cannot be searched yet.");
				}
				SearchMode mode = request.mode();
				String notice = null;
				float[] vector = null;
				if (mode != SearchMode.WORDS) {
					String unavailable = meaningUnavailable(serving);
					if (unavailable == null) {
						vector = query.vector();
						unavailable = query.problem();
					}
					if (unavailable == null && vector == null) {
						// The generation switched to one with vectors after the query was embedded.
						unavailable = "The meaning search is not available now; these hits come from words only.";
					}
					if (unavailable != null) {
						if (mode == SearchMode.MEANING) {
							return empty(mode, unavailable);
						}
						mode = SearchMode.WORDS;
						notice = unavailable;
					}
				}
				Query words = mode == SearchMode.MEANING ? null
					: HybridSearch.words(serving.index().analyzer(), request.query());
				if (words == null && vector == null) {
					return empty(mode, "The query has no word to search for.");
				}
				return page(serving.index(), words, vector, filter, request.limit(), security, mode, notice);
			});
		} catch (IOException ex) {
			throw new IllegalStateException("The index cannot be read: " + ex.getMessage(), ex);
		}
	}

	/**
	 * The query's vector, or why there is none.
	 */
	private record QueryVector(float[] vector, String problem) {
		// A plain carrier.
	}

	private QueryVector embed(SearchRequest request) {
		if (request.mode() == SearchMode.WORDS || _embedder == null || !_sync.servesVectors()) {
			return new QueryVector(null, null);
		}
		try {
			return new QueryVector(_embedder.embedQuery(request.query()), null);
		} catch (EmbeddingException ex) {
			return new QueryVector(null,
				"The meaning search is not available now (" + ex.getMessage() + "); these hits come from words only.");
		}
	}

	/**
	 * Searches for objects near the given one, in meaning and in their characteristic words.
	 *
	 * <p>
	 * Starts from the texts of the object the reader may read, never from an attribute hidden from
	 * them. The object itself is not a hit.
	 * </p>
	 *
	 * @param object
	 *        The object to start from; the reader must be allowed to read it.
	 * @param request
	 *        Types, scopes, filters, mode and limit; its words are ignored.
	 * @param security
	 *        The reader's rights.
	 * @throws SearchRefusedException
	 *         When the call runs in a system context or without a user.
	 * @throws IllegalArgumentException
	 *         When the reader may not read the object.
	 */
	public Hits similar(TLObject object, SearchRequest request, ModelSecurity security) {
		refuseWithoutReader();
		if (object == null || !security.canRead(object)) {
			throw new IllegalArgumentException("No such object.");
		}
		awaitIfAsked(request);
		String id = ReadFilter.idOf(object);
		IndexFilter filter = filter(request, security);
		if (filter == null) {
			return empty(request.mode(), "Nothing to search: none of the requested types is indexed.");
		}
		IndexFilter others = filter.excluding(Set.of(id));
		try {
			return _sync.withServing(serving -> {
				if (serving == null) {
					return empty(request.mode(), "The index is being built and cannot be searched yet.");
				}
				IndexGeneration index = serving.index();
				return index.search(searcher -> {
					List<StoredChunk> readable = readableChunks(searcher, object, id, security);
					if (readable.isEmpty()) {
						return empty(request.mode(), "The object has no indexed text you may read.");
					}
					SearchMode mode = request.mode();
					String notice = null;
					float[] vector = null;
					if (mode != SearchMode.WORDS) {
						String unavailable = meaningUnavailable(serving);
						if (unavailable == null) {
							int[] docs = readable.stream().mapToInt(StoredChunk::doc).toArray();
							vector = Vectors.mean(Vectors.readAll(searcher.getIndexReader(), IndexSchema.VECTOR, docs));
						}
						if (vector == null) {
							if (mode == SearchMode.MEANING) {
								return empty(mode, unavailable == null ? "The object has no vector." : unavailable);
							}
							mode = SearchMode.WORDS;
							notice = unavailable;
						}
					}
					Query words = null;
					if (mode != SearchMode.MEANING) {
						List<String> texts = new ArrayList<>();
						for (StoredChunk chunk : readable) {
							texts.add(chunk.text());
						}
						words = HybridSearch.like(searcher, index.analyzer(), texts);
					}
					if (words == null && vector == null) {
						return empty(mode, "The object's text has nothing to compare with.");
					}
					return page(index, words, vector, others, request.limit(), security, mode, notice);
				});
			});
		} catch (IOException ex) {
			throw new IllegalStateException("The index cannot be read: " + ex.getMessage(), ex);
		}
	}

	private List<StoredChunk> readableChunks(IndexSearcher searcher, TLObject object, String id, ModelSecurity security)
			throws IOException {
		IndexedType type = _types.forObject(object);
		List<StoredChunk> result = new ArrayList<>();
		if (type == null) {
			return result;
		}
		for (StoredChunk chunk : IndexGeneration.chunksOf(searcher, id)) {
			if (result.size() >= SIMILAR_CHUNKS) {
				break;
			}
			if (mayRead(object, type, chunk.attribute(), security)) {
				result.add(chunk);
			}
		}
		return result;
	}

	private void refuseWithoutReader() {
		if (ThreadContext.isSystemContext() || TLContext.currentUser() == null) {
			throw new SearchRefusedException("This search runs without a user account, in a system context where"
				+ " the application checks no rights. The index is only searched in the name of the user the run"
				+ " acts for.");
		}
	}

	private void awaitIfAsked(SearchRequest request) {
		if (request.minRevision() > 0) {
			_sync.awaitRevision(request.minRevision(), request.waitMillis());
		}
	}

	private String meaningUnavailable(IndexSync.Serving serving) {
		if (_embedder == null) {
			return "The meaning search is not configured or its model is not installed; these hits come from"
				+ " words only.";
		}
		if (!serving.vectors()) {
			return "The index is being rebuilt for another embedding model; until then it searches words only.";
		}
		return null;
	}

	/**
	 * The filter of the request inside the index, or <code>null</code> when it can find nothing.
	 */
	private IndexFilter filter(SearchRequest request, ModelSecurity security) {
		Set<String> types = new LinkedHashSet<>(request.types());
		Set<String> configured = new LinkedHashSet<>(_types.names());
		if (types.isEmpty()) {
			types.addAll(configured);
		}
		if (types.isEmpty()) {
			return null;
		}
		Map<String, Set<String>> keywords = request.filters();
		IndexFilter filter = new IndexFilter(types, null, keywords, Set.of());
		Set<String> readable = _scopes.of(security);
		if (readable == null) {
			if (request.scopes().isEmpty()) {
				return filter;
			}
			// Without the script, a requested scope is only narrowed to; it grants nothing.
			return filter.withScopes(request.scopes());
		}
		Set<String> unscoped = new HashSet<>();
		for (IndexedType type : _types.all()) {
			if (!type.scoped()) {
				unscoped.add(TLModelUtil.qualifiedName(type.type()));
			}
		}
		if (!request.scopes().isEmpty()) {
			readable.retainAll(request.scopes());
			unscoped.clear();
		}
		return filter.withScopes(readable, unscoped);
	}

	/**
	 * The page: searches, checks hit by hit in rank order, and searches deeper while the page is not
	 * full and the index has more.
	 */
	private Hits page(IndexGeneration index, Query words, float[] vector, IndexFilter filter, int limit,
			ModelSecurity security, SearchMode mode, String notice) throws IOException {
		Map<String, Hit> checked = new HashMap<>();
		int chunksRead = 0;
		int depth = Math.min(_maxCandidates, Math.max(MIN_DEPTH, limit * 3));
		while (true) {
			int fixedDepth = depth;
			HybridSearch.Result result = index.search(searcher -> HybridSearch.search(searcher,
				new HybridSearch.Request(words, vector, filter, fixedDepth, OVERFETCH, _fusion, _minSimilarity)));
			chunksRead += result.chunksRead();
			List<Hit> page = new ArrayList<>(limit);
			boolean unchecked = false;
			for (HybridSearch.ObjectHit candidate : result.hits()) {
				if (page.size() >= limit) {
					unchecked = true;
					break;
				}
				Hit hit;
				if (checked.containsKey(candidate.object())) {
					hit = checked.get(candidate.object());
				} else if (checked.size() >= _maxCandidates) {
					unchecked = true;
					break;
				} else {
					hit = check(candidate, filter.keywords().keySet(), security);
					checked.put(candidate.object(), hit);
				}
				if (hit != null) {
					page.add(hit);
				}
			}
			boolean more = unchecked || !result.exhausted();
			if (page.size() >= limit || result.exhausted() || depth >= _maxCandidates
				|| checked.size() >= _maxCandidates) {
				register(page);
				_lastChunksRead = chunksRead;
				IndexStatus status = _sync.status();
				return new Hits(List.copyOf(page), more, mode, status.indexedRevision(), status.pendingRevisions(),
					notice);
			}
			depth = Math.min(_maxCandidates, depth * 2);
		}
	}

	/**
	 * The hit for a candidate, or <code>null</code> when the reader may not see it.
	 *
	 * @param keywords
	 *        The keywords the search filters by: the reader must be allowed to read what they are
	 *        computed from.
	 */
	private Hit check(HybridSearch.ObjectHit candidate, Set<String> keywords, ModelSecurity security) {
		TLObject object = _resolver.resolve(candidate.object(), security);
		if (object == null) {
			return null;
		}
		IndexedType type = _types.forObject(object);
		StoredChunk chunk = candidate.chunk();
		if (type == null || !mayRead(object, type, chunk.attribute(), security)) {
			return null;
		}
		for (String keyword : keywords) {
			if (!mayReadAll(object, type.keywordAttributes(keyword, object), security)) {
				return null;
			}
		}
		return new Hit(object, candidate.object(), MetaLabelProvider.INSTANCE.getLabel(object),
			TLModelUtil.qualifiedName(object.tType()), chunk.attribute(), chunk.text(), chunk.locator(),
			candidate.wordRank(), candidate.meaningRank(), candidate.cosine(), candidate.score(),
			stale(object, chunk));
	}

	private static boolean mayRead(TLObject object, IndexedType type, String part, ModelSecurity security) {
		IndexedType.Part text = type.part(part);
		if (text == null) {
			return false;
		}
		return mayReadAll(object, text.readAttributes(object), security);
	}

	private static boolean mayReadAll(TLObject object, List<TLStructuredTypePart> attributes,
			ModelSecurity security) {
		if (attributes == null || attributes.isEmpty()) {
			return false;
		}
		for (TLStructuredTypePart attribute : attributes) {
			if (!security.canRead(object, attribute)) {
				return false;
			}
		}
		return true;
	}

	private boolean stale(TLObject object, StoredChunk chunk) {
		KnowledgeItem item = object.tHandle();
		return item != null && _kb.getHistoryManager().getLastUpdate(item) > chunk.revision();
	}

	/**
	 * Puts each hit on the run's reading list with the text the reader was shown; several chunks of
	 * one object are kept together, so a quote from any of them passes the citation check.
	 */
	private static void register(List<Hit> hits) {
		ReadingList list = ReadingListBinding.current();
		if (list == null) {
			return;
		}
		for (Hit hit : hits) {
			ReadSource known = list.find(hit.id());
			String text = hit.text();
			if (known != null && !known.text().isEmpty()) {
				text = known.text().contains(hit.text()) ? known.text() : known.text() + "\n[...]\n" + hit.text();
			}
			list.add(ReadSource.of(CitationKinds.objectKind(hit.object()), hit.id(), hit.label()).withText(text));
		}
	}

	private Hits empty(SearchMode mode, String notice) {
		IndexStatus status = _sync.status();
		return new Hits(List.of(), false, mode, status.indexedRevision(), status.pendingRevisions(), notice);
	}

}
