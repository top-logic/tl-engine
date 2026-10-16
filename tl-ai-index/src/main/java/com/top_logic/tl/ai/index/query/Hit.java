/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.index.lucene.Locator;

/**
 * One object found, with the chunk that found it.
 *
 * <p>
 * Only a hit the reader may read - the object and every attribute its chunk's text comes from - is
 * ever built, so its text may be shown.
 * </p>
 *
 * @param object
 *        The object.
 * @param id
 *        Its identifier, {@code "Type:id"}.
 * @param label
 *        Its label.
 * @param type
 *        The qualified name of its type.
 * @param part
 *        The text of the object the chunk comes from: the attribute name or the configured name.
 * @param text
 *        The chunk's text.
 * @param locator
 *        Where the chunk lies in that text, with its page.
 * @param wordRank
 *        The object's rank on the word side, from 1; 0 when words did not find it.
 * @param meaningRank
 *        The object's rank on the meaning side, from 1; 0 when meaning did not find it.
 * @param cosine
 *        The cosine of the chunk to the query; {@link Float#NaN} when meaning did not find it.
 * @param score
 *        The fused score.
 * @param stale
 *        Whether the object changed after the chunk was indexed; its new state is on its way.
 */
public record Hit(TLObject object, String id, String label, String type, String part, String text, Locator locator,
		int wordRank, int meaningRank, float cosine, double score, boolean stale) {
	// A plain carrier.
}
