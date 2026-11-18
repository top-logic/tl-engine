/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider;

/**
 * The API families a model can belong to, as answered by
 * {@link LLMProviderConfig#getApiFamily(String)}.
 *
 * <p>
 * The family decides which vendor-specific options a model is given (e.g. a thinking effort for
 * {@link #ANTHROPIC}, a reasoning effort for {@link #OPENAI}), independent of the provider that
 * serves it: Claude is of the family {@link #ANTHROPIC} on the Anthropic API as on AWS Bedrock or
 * Google Vertex AI. A family is spelled like the catalog provider ID of the vendor's own API.
 * </p>
 */
public final class ApiFamilies {

	/** The models of Anthropic (Claude). */
	public static final String ANTHROPIC = "anthropic";

	/** The models of <code>OpenAI</code>. */
	public static final String OPENAI = "openai";

	/** The models of <code>xAI</code>. */
	public static final String XAI = "xai";

	private ApiFamilies() {
		// Constants only.
	}
}
