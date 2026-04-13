/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

/**
 * Controls the format of the LLM response.
 */
public enum ResponseFormat {

	/** Plain text response (default). */
	TEXT,

	/** Structured JSON object response. */
	JSON,

}
