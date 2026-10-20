/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.index.embed.AbstractEmbedder;
import com.top_logic.tl.ai.index.embed.EmbeddingException;

/**
 * An embedder that cannot be created leaves the index searching words only, and the application
 * starts.
 */
public class TestAiIndexServiceEmbedder extends TestCase {

	/**
	 * A native library that does not load - ONNX Runtime on a musl image, say - is an {@link Error},
	 * not an exception.
	 */
	public void testNativeLibraryThatDoesNotLoad() {
		AiIndexService service = new AiIndexService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			config(NativeLessEmbedder.class));

		String problem = service.embedderProblem();
		assertNotNull("The service must say why it searches words only.", problem);
		assertTrue(problem, problem.contains("libnative-less.so"));
	}

	/** A model whose files are not there is refused with an exception. */
	public void testModelThatIsNotThere() {
		AiIndexService service = new AiIndexService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			config(MissingModelEmbedder.class));

		String problem = service.embedderProblem();
		assertNotNull("The service must say why it searches words only.", problem);
		assertTrue(problem, problem.contains("model.onnx"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static AiIndexService.Config<?> config(Class<? extends AbstractEmbedder<?>> embedder) {
		AiIndexService.Config<?> config = TypedConfiguration.newConfigItem(AiIndexService.Config.class);
		AbstractEmbedder.Config embedderConfig = TypedConfiguration.newConfigItem(AbstractEmbedder.Config.class);
		embedderConfig.setImplementationClass(embedder);
		embedderConfig.update(embedderConfig.descriptor().getProperty(AbstractEmbedder.Config.DIMENSIONS), 8);
		config.update(config.descriptor().getProperty(AiIndexConfig.EMBEDDER), embedderConfig);
		return config;
	}

	/**
	 * An embedder whose native library does not load, as {@link System#loadLibrary(String)} reports
	 * it.
	 */
	public static class NativeLessEmbedder extends FailingEmbedder {

		/**
		 * Fails as a native library that does not load.
		 */
		@CalledByReflection
		public NativeLessEmbedder(InstantiationContext context, Config<?> config) {
			super(context, config);
			throw new UnsatisfiedLinkError(
				"/tmp/libnative-less.so: Error loading shared library libstdc++.so.6: No such file or directory");
		}
	}

	/**
	 * An embedder whose model files are not there.
	 */
	public static class MissingModelEmbedder extends FailingEmbedder {

		/**
		 * Fails as a model that is not installed.
		 */
		@CalledByReflection
		public MissingModelEmbedder(InstantiationContext context, Config<?> config) {
			super(context, config);
			throw new IllegalArgumentException("The embedding model needs /models/model.onnx.");
		}
	}

	/**
	 * Base of the embedders above; none of them is ever asked to embed.
	 */
	public abstract static class FailingEmbedder extends AbstractEmbedder<AbstractEmbedder.Config<?>> {

		/**
		 * Creates the embedder.
		 */
		protected FailingEmbedder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public String modelId() {
			return "failing";
		}

		@Override
		protected List<float[]> embedBatch(List<String> texts) throws EmbeddingException {
			throw new EmbeddingException("Not created.");
		}
	}

	/** The services the {@link AiIndexService} constructor reads from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestAiIndexServiceEmbedder.class)));
	}

}
