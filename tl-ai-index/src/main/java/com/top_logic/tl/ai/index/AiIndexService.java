/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index;

import java.io.IOException;
import java.nio.file.Path;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.convert.FormatConverterFactory;
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
	// The text of uploaded files.
	FormatConverterFactory.Module.class,
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

	private final String _embedderProblem;

	private AiIndex _index;

	/**
	 * Creates the service from configuration.
	 *
	 * <p>
	 * An embedder that cannot be created - an in-process model whose files are not installed, or
	 * whose native library does not load on this system - leaves the index searching words only,
	 * and says why in its status and in the log. The application starts either way.
	 * </p>
	 */
	public AiIndexService(InstantiationContext context, Config<?> config) {
		super(context, config);
		Embedder embedder = null;
		String problem = null;
		if (config.getEmbedder() != null) {
			try {
				embedder = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config.getEmbedder());
			} catch (RuntimeException | LinkageError ex) {
				// A native library that does not load is a LinkageError, not an exception.
				problem = "The embedder cannot be created, the index searches words only: " + reason(ex);
				Logger.error(problem, ex, AiIndexService.class);
			}
		}
		_embedder = embedder;
		_embedderProblem = problem;
	}

	/**
	 * The message of what the embedder's constructor threw: the instantiation wraps it into
	 * configuration problems that only say that the instantiation failed.
	 */
	private static String reason(Throwable ex) {
		Throwable reason = ex;
		while ((reason instanceof ConfigurationError || reason instanceof ConfigurationException)
			&& reason.getCause() != null) {
			reason = reason.getCause();
		}
		return reason.getMessage();
	}

	/**
	 * Why the configured embedder is not used, or <code>null</code> when it is.
	 */
	public String embedderProblem() {
		return _embedderProblem;
	}

	@Override
	protected void startUp() {
		super.startUp();
		Path root = Path.of(AliasManager.getInstance().replace(getConfig().getLocation()));
		AiIndex index = new AiIndex(getConfig(), _embedder, root, PersistencyLayer.getKnowledgeBase());
		try {
			index.start();
		} catch (IOException | RuntimeException ex) {
			// The service is not shut down after a failed start, and a restart creates a new embedder.
			closeEmbedder();
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
		closeEmbedder();
		super.shutDown();
	}

	private void closeEmbedder() {
		if (_embedder != null) {
			_embedder.close();
		}
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
