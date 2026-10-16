/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

/**
 * Where a chunk lies in the text of its attribute.
 *
 * @param chunk
 *        The number of the chunk in its attribute, from 0.
 * @param start
 *        The offset of the chunk's first character in the attribute's text.
 * @param end
 *        The offset behind the chunk's last character.
 * @param page
 *        The page the chunk is on, from a page marker of the text, or 0 when the text has none. A
 *        chunk never crosses a page.
 */
public record Locator(int chunk, int start, int end, int page) {

	/**
	 * The stored form, read back by {@link #parse(String)}.
	 */
	public String format() {
		return chunk + ":" + start + ":" + end + ":" + page;
	}

	/**
	 * Reads the stored form of {@link #format()}.
	 */
	public static Locator parse(String stored) {
		String[] parts = stored.split(":");
		return new Locator(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
			Integer.parseInt(parts[3]));
	}

}
