/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import org.apache.lucene.document.Document;

/**
 * What the index stored of one chunk.
 *
 * @param doc
 *        The chunk's document number in the searcher it was read from.
 * @param object
 *        The object the chunk belongs to.
 * @param chunkId
 *        The chunk's identifier.
 * @param attribute
 *        The attribute the chunk's text came from.
 * @param locator
 *        Where the text lies in the attribute.
 * @param revision
 *        The revision of the object that was indexed.
 * @param text
 *        The chunk's text.
 * @param textHash
 *        Hash of the embedded text, or <code>null</code> when the chunk has no vector.
 * @param model
 *        The embedding model of the chunk's vector, or <code>null</code> when it has none.
 */
public record StoredChunk(int doc, String object, String chunkId, String attribute, Locator locator, long revision,
		String text, String textHash, String model) {

	/**
	 * Reads the stored fields of a chunk document.
	 */
	public static StoredChunk read(int doc, Document stored) {
		return new StoredChunk(doc, stored.get(IndexSchema.OBJECT), stored.get(IndexSchema.CHUNK),
			stored.get(IndexSchema.ATTRIBUTE), Locator.parse(stored.get(IndexSchema.LOCATOR)),
			stored.getField(IndexSchema.REVISION).numericValue().longValue(), stored.get(IndexSchema.TEXT),
			stored.get(IndexSchema.TEXT_HASH), stored.get(IndexSchema.MODEL));
	}

}
