/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.util.Collection;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.VectorSimilarityFunction;

/**
 * One chunk of one object, as it is written into the index.
 *
 * @param object
 *        The object, as {@code ObjectKey.asString()}, e.g. {@code Statement:4711}.
 * @param attribute
 *        The name of the attribute the text came from.
 * @param locator
 *        Where the text lies in the attribute.
 * @param types
 *        The qualified names of the object's type and of its super types.
 * @param scopes
 *        The identifiers of the objects that scope the object; empty without a scope rule.
 * @param keywords
 *        The application's keyword values by keyword name.
 * @param revision
 *        The revision of the object that was indexed.
 * @param text
 *        The chunk's text: what the word fields analyse and what a hit shows.
 * @param textHash
 *        Hash of the text that was embedded (which may carry a prefix the chunk text does not).
 * @param model
 *        The embedding model that computed {@code vector}; <code>null</code> without a vector.
 * @param vector
 *        The normalized embedding, or <code>null</code> when the chunk has none (word mode).
 */
public record ChunkDocument(String object, String attribute, Locator locator, Collection<String> types,
		Collection<String> scopes, Map<String, ? extends Collection<String>> keywords, long revision, String text,
		String textHash, String model, float[] vector) {

	/**
	 * The identifier of the chunk, unique in the index.
	 */
	public String chunkId() {
		return object + "#" + attribute + "#" + locator.chunk();
	}

	/**
	 * The Lucene document of this chunk.
	 */
	public Document toDocument() {
		Document doc = new Document();
		doc.add(new StringField(IndexSchema.OBJECT, object, Store.YES));
		doc.add(new StringField(IndexSchema.CHUNK, chunkId(), Store.YES));
		for (String type : types) {
			doc.add(new StringField(IndexSchema.TYPE, type, Store.NO));
		}
		for (String scope : scopes) {
			doc.add(new StringField(IndexSchema.SCOPE, scope, Store.NO));
		}
		for (Map.Entry<String, ? extends Collection<String>> keyword : keywords.entrySet()) {
			String field = IndexSchema.keywordField(keyword.getKey());
			for (String value : keyword.getValue()) {
				doc.add(new StringField(field, value, Store.NO));
			}
		}
		doc.add(new StringField(IndexSchema.ATTRIBUTE, attribute, Store.YES));
		doc.add(new StoredField(IndexSchema.LOCATOR, locator.format()));
		doc.add(new LongPoint(IndexSchema.REVISION, revision));
		doc.add(new NumericDocValuesField(IndexSchema.REVISION, revision));
		doc.add(new StoredField(IndexSchema.REVISION, revision));
		doc.add(new StoredField(IndexSchema.TEXT, text));
		doc.add(new TextField(IndexSchema.TEXT_DE, text, Store.NO));
		doc.add(new TextField(IndexSchema.TEXT_EN, text, Store.NO));
		if (vector != null) {
			doc.add(new StringField(IndexSchema.TEXT_HASH, textHash, Store.YES));
			doc.add(new StringField(IndexSchema.MODEL, model, Store.YES));
			doc.add(new KnnFloatVectorField(IndexSchema.VECTOR, Vectors.normalized(vector),
				VectorSimilarityFunction.DOT_PRODUCT));
		}
		return doc;
	}

}
