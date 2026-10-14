/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

/**
 * Which side of the index a search asks.
 */
public enum SearchMode {

	/** Words and meaning, fused. */
	HYBRID,

	/** Words only: exact terms, codes, names. */
	WORDS,

	/** Meaning only: paraphrases, other languages. */
	MEANING;

}
