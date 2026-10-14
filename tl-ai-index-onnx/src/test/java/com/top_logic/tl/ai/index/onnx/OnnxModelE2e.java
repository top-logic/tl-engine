/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.onnx;

import org.junit.Assume;

/**
 * The switch that turns the tests with a real embedding model on, and the {@link Assume assumption}
 * that makes a run without it report as <b>skipped</b>.
 *
 * <p>
 * The model files are hundreds of megabytes that no build downloads; they lie in a directory a
 * developer or an operator fills from the model's official repository.
 * </p>
 *
 * <h2>Running the gated test locally</h2>
 *
 * <pre>
 * mvn -pl tl-ai-index-onnx test -DskipTests=false -Dtl.ai.index.model.e2e=true \
 *   -Dtl.ai.index.model.dir=$HOME/.cache/tl-ai-index-models/snowflake-arctic-embed-m-v2.0
 * </pre>
 *
 * <p>
 * Further properties: {@code tl.ai.index.model.file} (default {@code model_int8.onnx}),
 * {@code tl.ai.index.model.pooling} ({@code CLS}), {@code tl.ai.index.model.dimensions} (768),
 * {@code tl.ai.index.model.query-prefix} ({@code "query: "}), {@code tl.ai.index.model.passage-prefix}
 * (empty). For multilingual-e5-small: {@code MEAN}, 384, {@code "query: "} and {@code "passage: "}.
 * </p>
 */
public class OnnxModelE2e {

	/**
	 * System property that enables the tests with a real model.
	 */
	public static final String ENABLED_PROPERTY = "tl.ai.index.model.e2e";

	/**
	 * Skips the calling test unless {@link #ENABLED_PROPERTY} is set.
	 */
	public static void assumeEnabled() {
		Assume.assumeTrue("embedding model tests are off, enable with -D" + ENABLED_PROPERTY + "=true",
			Boolean.getBoolean(ENABLED_PROPERTY));
	}

}
