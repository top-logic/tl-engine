/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tl.ai.index.config.ChunkingConfig;
import com.top_logic.tl.ai.index.config.IndexedTypeConfig;
import com.top_logic.tl.ai.index.embed.Embedder;

/**
 * What an index holds and how it is kept.
 */
public interface AiIndexConfig extends ConfigurationItem {

	/** Configuration name of {@link #getLocation()}. */
	String LOCATION = "location";

	/** Configuration name of {@link #getEmbedder()}. */
	String EMBEDDER = "embedder";

	/** Configuration name of {@link #getChunking()}. */
	String CHUNKING = "chunking";

	/** Configuration name of {@link #getTypes()}. */
	String TYPES = "types";

	/** Configuration name of {@link #getReadableScopes()}. */
	String READABLE_SCOPES = "readable-scopes";

	/** Configuration name of {@link #getMinSimilarity()}. */
	String MIN_SIMILARITY = "min-similarity";

	/** Configuration name of {@link #getMaxCandidates()}. */
	String MAX_CANDIDATES = "max-candidates";

	/** Configuration name of {@link #getBatchSize()}. */
	String BATCH_SIZE = "batch-size";

	/** Configuration name of {@link #getRevisionSlice()}. */
	String REVISION_SLICE = "revision-slice";

	/** Configuration name of {@link #getBuildStepMillis()}. */
	String BUILD_STEP_MILLIS = "build-step-millis";

	/**
	 * The directory of the index, below which each generation has a directory of its own.
	 *
	 * <p>
	 * The index holds the text of every indexed object, so the directory needs the same protection
	 * as the database files. Each node of a cluster keeps its own index; the directory must not be
	 * shared.
	 * </p>
	 *
	 * @see #LOCATION
	 */
	@Name(LOCATION)
	@StringDefault("%VAR_PATH%/tl-ai-index")
	String getLocation();

	/**
	 * How texts are turned into vectors; none for an index of words only.
	 *
	 * @see #EMBEDDER
	 */
	@Name(EMBEDDER)
	@Nullable
	PolymorphicConfiguration<? extends Embedder> getEmbedder();

	/**
	 * How texts are cut into chunks.
	 *
	 * @see #CHUNKING
	 */
	@Name(CHUNKING)
	@ItemDefault
	ChunkingConfig getChunking();

	/**
	 * The indexed types.
	 *
	 * @see #TYPES
	 */
	@Name(TYPES)
	@EntryTag("type")
	List<IndexedTypeConfig> getTypes();

	/**
	 * TL-Script without arguments answering every scope object a reader may search in, e.g.
	 * {@code all(`my:Project`)}.
	 *
	 * <p>
	 * Runs in the reader's own interaction for each search, and the result is filtered again by the
	 * reader's read rights. The search then filters by these scopes inside the index, so a reader of
	 * one project of a thousand gets a full page from that project. Without it, chunks of scoped
	 * types are filtered by type only and checked one by one, which can leave a narrow reader with a
	 * short page.
	 * </p>
	 *
	 * @see #READABLE_SCOPES
	 */
	@Name(READABLE_SCOPES)
	@Nullable
	Expr getReadableScopes();

	/**
	 * The lowest cosine between the query and a chunk that finds the chunk by meaning.
	 *
	 * <p>
	 * Without it, a search answers the nearest chunks also when none of them is near: a query that
	 * nothing in the index means gets unrelated objects. The value depends on the model: measure the
	 * cosine of related and of unrelated texts of the application and put it between. 0 switches it
	 * off.
	 * </p>
	 *
	 * @see #MIN_SIMILARITY
	 */
	@Name(MIN_SIMILARITY)
	float getMinSimilarity();

	/**
	 * How many chunks each side of a search reads at most, when it searches deeper because the
	 * reader may not read the best hits.
	 *
	 * @see #MAX_CANDIDATES
	 */
	@Name(MAX_CANDIDATES)
	@IntDefault(2000)
	int getMaxCandidates();

	/**
	 * How many objects are indexed together, with one call of the embedder.
	 *
	 * @see #BATCH_SIZE
	 */
	@Name(BATCH_SIZE)
	@IntDefault(64)
	int getBatchSize();

	/**
	 * How many revisions of the history are read and committed at once.
	 *
	 * @see #REVISION_SLICE
	 */
	@Name(REVISION_SLICE)
	@IntDefault(1000)
	int getRevisionSlice();

	/**
	 * How long one step of a full build works before it commits and the worker looks at new
	 * commits.
	 *
	 * @see #BUILD_STEP_MILLIS
	 */
	@Name(BUILD_STEP_MILLIS)
	@LongDefault(10_000)
	long getBuildStepMillis();

}
