/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

/**
 * HTTP authentication scheme used when sending requests to an LLM provider endpoint.
 *
 * <p>{@link #BEARER} covers the standard <code>OpenAI</code>-style {@code Authorization: Bearer <key>} header.
 * {@link #API_KEY} covers the Azure <code>OpenAI</code> convention of sending the key in a plain
 * {@code api-key} header without a scheme prefix.</p>
 */
public enum AuthScheme {

	/**
	 * Standard OAuth2 bearer token: {@code Authorization: Bearer <key>}.
	 *
	 * <p>Used by <code>OpenAI</code>, Mistral, <code>xAI</code>, and Azure-compatible gateways such as Nexus that accept
	 * bearer tokens in the standard header.</p>
	 */
	BEARER,

	/**
	 * Azure <code>OpenAI</code> subscription key: {@code api-key: <key>}.
	 *
	 * <p>Used when connecting directly to an Azure <code>OpenAI</code> resource endpoint that requires the
	 * subscription key in the {@code api-key} header rather than an {@code Authorization} bearer
	 * token.</p>
	 */
	API_KEY,
}
