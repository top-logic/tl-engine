/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.util.Version;

/**
 * The fields of a chunk document and how the word fields are analysed.
 *
 * <p>
 * One Lucene document holds one chunk of one object. A short object has one chunk, a long text many.
 * Every chunk carries the filter fields of its object (type, scope, keywords), so a filter inside the
 * query sees every chunk.
 * </p>
 */
public final class IndexSchema {

	/**
	 * The version of the document layout.
	 *
	 * <p>
	 * Part of every generation's fingerprint: a change of a field or of an analyzer must raise it, so
	 * that the index is built again rather than read with a layout it was not written with.
	 * </p>
	 */
	public static final int SCHEMA_VERSION = 1;

	/** The object a chunk belongs to, as {@code ObjectKey.asString()}. Term of replace and delete. */
	public static final String OBJECT = "object";

	/** The identifier of a chunk: {@code <object>#<attribute>#<n>}. */
	public static final String CHUNK = "chunk";

	/** The qualified name of the object's type and of each of its super types. */
	public static final String TYPE = "type";

	/** The identifiers of the objects that scope the chunk's object. */
	public static final String SCOPE = "scope";

	/** Prefix of an application keyword field, e.g. {@code kw.status}. */
	public static final String KEYWORD_PREFIX = "kw.";

	/** The attribute the chunk's text came from; the reader must be allowed to read it. */
	public static final String ATTRIBUTE = "attr";

	/** Where in the attribute's text the chunk lies, see {@link Locator}. */
	public static final String LOCATOR = "loc";

	/** The revision of the object that was indexed. */
	public static final String REVISION = "rev";

	/** The chunk text itself, for the snippet and for the citation check. */
	public static final String TEXT = "text";

	/** Hash of the text that was embedded, to reuse a vector when the text did not change. */
	public static final String TEXT_HASH = "text_hash";

	/** The embedding model that computed the chunk's vector. */
	public static final String MODEL = "model";

	/** The chunk text, analysed for German. */
	public static final String TEXT_DE = "text_de";

	/** The chunk text, analysed for English. */
	public static final String TEXT_EN = "text_en";

	/** The normalized embedding of the chunk. */
	public static final String VECTOR = "vec";

	/** The word fields a query asks, in one disjunction. */
	public static final String[] WORD_FIELDS = { TEXT_DE, TEXT_EN };

	private IndexSchema() {
		// Constants only.
	}

	/**
	 * The field of an application keyword.
	 */
	public static String keywordField(String name) {
		return KEYWORD_PREFIX + name;
	}

	/**
	 * The analyzer of the index: German for {@link #TEXT_DE}, English for {@link #TEXT_EN}, none for
	 * every other field.
	 *
	 * <p>
	 * {@link GermanAnalyzer} is standard tokenizer, lower case, German stop words,
	 * {@code GermanNormalizationFilter} and {@code GermanLightStemFilter} - the chain the engine's
	 * word index uses. It splits no compounds: no compound data with a licence this product can
	 * ship exists, and the meaning side covers many compounds.
	 * </p>
	 */
	public static Analyzer analyzer() {
		return new PerFieldAnalyzerWrapper(new KeywordAnalyzer(),
			Map.of(TEXT_DE, new GermanAnalyzer(), TEXT_EN, new EnglishAnalyzer()));
	}

	/**
	 * The major version of the Lucene that writes the index; part of the fingerprint.
	 */
	public static int luceneMajor() {
		return Version.LATEST.major;
	}

}
