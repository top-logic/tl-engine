/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index;

import java.io.IOException;
import java.nio.file.Path;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.tl.ai.index.config.ChunkingConfig;
import com.top_logic.tl.ai.index.config.IndexedTypeConfig;
import com.top_logic.tl.ai.index.config.IndexedTypes;
import com.top_logic.tl.ai.index.embed.Embedder;
import com.top_logic.tl.ai.index.lucene.Generations;
import com.top_logic.tl.ai.index.sync.IndexStatus;
import com.top_logic.tl.ai.index.sync.IndexSync;

/**
 * One hybrid index of the application's objects: what it holds, how it is kept in step, and how it
 * is searched.
 *
 * <p>
 * {@link AiIndexService} holds the application's index; a test creates its own over a directory of
 * its own.
 * </p>
 */
public final class AiIndex {

	private final AiIndexConfig _config;

	private final Embedder _embedder;

	private final IndexedTypes _types;

	private final IndexSync _sync;

	/**
	 * Creates an index; {@link #start()} opens it.
	 *
	 * @param config
	 *        What it holds.
	 * @param embedder
	 *        The embedder, or <code>null</code> for words only.
	 * @param root
	 *        Its directory.
	 * @param kb
	 *        The knowledge base it follows.
	 */
	public AiIndex(AiIndexConfig config, Embedder embedder, Path root, KnowledgeBase kb) {
		_config = config;
		_embedder = embedder;
		_types = new IndexedTypes(config.getTypes());
		_sync = new IndexSync(new IndexSync.Settings(kb, root, _types, ChunkingConfig.chunker(config.getChunking()),
			config.getChunking().getMaxTextChars(), embedder, fingerprint(config, embedder), config.getBatchSize(),
			config.getRevisionSlice(), config.getBuildStepMillis()));
	}

	/**
	 * The fingerprint of everything the content of the index depends on: the types, the chunking and
	 * the embedder.
	 */
	public static String fingerprint(AiIndexConfig config, Embedder embedder) {
		StringBuilder types = new StringBuilder();
		for (IndexedTypeConfig type : config.getTypes()) {
			types.append(TypedConfiguration.toString("type", type)).append('\n');
		}
		return Generations.fingerprint(types.toString(), TypedConfiguration.toString("chunking", config.getChunking()),
			embedder == null ? "words" : embedder.fingerprint());
	}

	/**
	 * Opens the index and starts keeping it in step.
	 */
	public void start() throws IOException {
		_sync.start();
	}

	/**
	 * Stops keeping the index in step and closes it.
	 */
	public void stop() {
		_sync.stop();
	}

	/**
	 * The configuration.
	 */
	public AiIndexConfig config() {
		return _config;
	}

	/**
	 * The embedder, or <code>null</code> for words only.
	 */
	public Embedder embedder() {
		return _embedder;
	}

	/**
	 * The indexed types.
	 */
	public IndexedTypes types() {
		return _types;
	}

	/**
	 * The synchronization with the knowledge base.
	 */
	public IndexSync sync() {
		return _sync;
	}

	/**
	 * Where the index stands.
	 */
	public IndexStatus status() {
		return _sync.status();
	}

	/**
	 * Waits until the index holds every change up to the given revision.
	 *
	 * @return Whether it does.
	 */
	public boolean awaitRevision(long revision, long timeoutMillis) {
		return _sync.awaitRevision(revision, timeoutMillis);
	}

}
