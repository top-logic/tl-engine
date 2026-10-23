/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

/**
 * Controls how the LLM selects tools during a request.
 */
public enum ToolChoice {

	/** The LLM decides whether to call a tool (default). */
	AUTO,

	/** The LLM must call at least one tool. */
	REQUIRED,

	/** The LLM must not call any tool. */
	NONE,

}
