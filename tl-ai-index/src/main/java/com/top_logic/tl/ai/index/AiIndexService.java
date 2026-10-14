/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index;

import java.io.IOException;
import java.nio.file.Path;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.tl.ai.index.embed.Embedder;
import com.top_logic.util.model.ModelService;

/**
 * The application's hybrid index: words and meaning of the configured types, kept in step with the
 * knowledge base, searched under the reader's rights.
 *
 * <p>
 * Configure the indexed types and an embedder; without types the index holds nothing, without an
 * embedder it searches words only. See {@code tl-ai-index/README.md}.
 * </p>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	ModelService.Module.class,
})
@Label("AI index")
public class AiIndexService extends ConfiguredManagedClass<AiIndexService.Config<?>> {

	/**
	 * Configuration of the {@link AiIndexService}.
	 */
	public interface Config<I extends AiIndexService> extends ConfiguredManagedClass.Config<I>, AiIndexConfig {
		// Everything is in AiIndexConfig.
	}

	private final Embedder _embedder;

	private AiIndex _index;

	/**
	 * Creates the service from configuration.
	 */
	public AiIndexService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_embedder = config.getEmbedder() == null ? null : context.getInstance(config.getEmbedder());
	}

	@Override
	protected void startUp() {
		super.startUp();
		Path root = Path.of(AliasManager.getInstance().replace(getConfig().getLocation()));
		AiIndex index = new AiIndex(getConfig(), _embedder, root, PersistencyLayer.getKnowledgeBase());
		try {
			index.start();
		} catch (IOException ex) {
			throw new RuntimeException("Cannot open the AI index in " + root + ": " + ex.getMessage(), ex);
		}
		_index = index;
	}

	@Override
	protected void shutDown() {
		if (_index != null) {
			_index.stop();
			_index = null;
		}
		if (_embedder != null) {
			_embedder.close();
		}
		super.shutDown();
	}

	/**
	 * The index of the application.
	 *
	 * @throws IllegalStateException
	 *         When the service is not running.
	 */
	public AiIndex index() {
		AiIndex index = _index;
		if (index == null) {
			throw new IllegalStateException("The AI index is not running.");
		}
		return index;
	}

	/**
	 * The running service.
	 */
	public static AiIndexService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module of the {@link AiIndexService}.
	 */
	public static final class Module extends TypedRuntimeModule<AiIndexService> {

		/** The module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton.
		}

		@Override
		public Class<AiIndexService> getImplementation() {
			return AiIndexService.class;
		}
	}

}
