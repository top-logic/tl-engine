/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.config;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Which objects of one type are indexed, with which text, scope and keywords.
 *
 * <pre>
 * &lt;type name="pietro:Statement" scope="s -&gt; $s.get(`pietro:Statement#project`)"&gt;
 *     &lt;text attribute="text"/&gt;
 *     &lt;text name="context" script="s -&gt; $s.get(`pietro:Statement#title`)" reads="title"/&gt;
 *     &lt;keywords&gt;
 *         &lt;keyword name="kind" script="s -&gt; $s.get(`pietro:Statement#kind`).get(`name`)"/&gt;
 *     &lt;/keywords&gt;
 * &lt;/type&gt;
 * </pre>
 *
 * <p>
 * The configuration applies to the type and to every sub type that has no configuration of its own.
 * The scripts run in a system interaction while the index is written, so they see every object; they
 * must only read.
 * </p>
 */
public interface IndexedTypeConfig extends ConfigurationItem {

	/** Configuration name of {@link #getName()}. */
	String NAME = "name";

	/** Configuration name of {@link #getTexts()}. */
	String TEXTS = "texts";

	/** Configuration name of {@link #getScope()}. */
	String SCOPE = "scope";

	/** Configuration name of {@link #getKeywords()}. */
	String KEYWORDS = "keywords";

	/** Configuration name of {@link #getLabelPrefix()}. */
	String LABEL_PREFIX = "label-prefix";

	/**
	 * The qualified name of the type, e.g. {@code pietro:Statement}.
	 *
	 * @see #NAME
	 */
	@Name(NAME)
	@Mandatory
	String getName();

	/**
	 * The texts of an object of the type that are searched.
	 *
	 * @see #TEXTS
	 */
	@Name(TEXTS)
	@EntryTag("text")
	@DefaultContainer
	List<TextPart> getTexts();

	/**
	 * TL-Script of one argument, the object, answering the object or objects that scope it, e.g. its
	 * project.
	 *
	 * <p>
	 * A search filters by the scopes the reader may read inside the index, so a reader who may read
	 * one project of a thousand gets a full page from that project. Without a scope the search
	 * filters by type only and checks the rights of each hit afterwards, which may leave a narrow
	 * reader with a short page.
	 * </p>
	 *
	 * @see #SCOPE
	 */
	@Name(SCOPE)
	@Nullable
	Expr getScope();

	/**
	 * Values a search can filter by, e.g. a record kind or a status.
	 *
	 * @see #KEYWORDS
	 */
	@Name(KEYWORDS)
	@EntryTag("keyword")
	@Key(Keyword.NAME)
	List<Keyword> getKeywords();

	/**
	 * Whether the object's label is put in front of every chunk before it is embedded.
	 *
	 * <p>
	 * So a chunk from page 40 still carries the document's title in its vector. The label is not
	 * part of the chunk's text that the word fields analyse and a hit shows.
	 * </p>
	 *
	 * @see #LABEL_PREFIX
	 */
	@Name(LABEL_PREFIX)
	@BooleanDefault(true)
	boolean getLabelPrefix();

	/**
	 * One text of an indexed object: the value of an attribute, or what a script derives.
	 */
	interface TextPart extends ConfigurationItem {

		/** Configuration name of {@link #getPartName()}. */
		String NAME = "name";

		/** Configuration name of {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name of {@link #getScript()}. */
		String SCRIPT = "script";

		/** Configuration name of {@link #getReads()}. */
		String READS = "reads";

		/**
		 * The name of the part, stored with each chunk; the attribute's name when none is given.
		 *
		 * @see #NAME
		 */
		@Name(NAME)
		@Nullable
		String getPartName();

		/**
		 * The attribute whose value is the text: a string, an internationalized text, a binary
		 * whose content is converted to text, or a list of those.
		 *
		 * @see #ATTRIBUTE
		 */
		@Name(ATTRIBUTE)
		@Nullable
		String getAttribute();

		/**
		 * TL-Script of one argument, the object, answering the text, instead of an attribute.
		 *
		 * @see #SCRIPT
		 */
		@Name(SCRIPT)
		@Nullable
		Expr getScript();

		/**
		 * The attributes of the object the {@link #getScript() script} reads.
		 *
		 * <p>
		 * A hit shows a chunk only to a reader who may read each of these attributes. A script part
		 * without this list shows its text to nobody.
		 * </p>
		 *
		 * @see #READS
		 */
		@Name(READS)
		@Format(CommaSeparatedStrings.class)
		List<String> getReads();
	}

	/**
	 * A value a search can filter by.
	 */
	interface Keyword extends ConfigurationItem {

		/** Configuration name of {@link #getName()}. */
		String NAME = "name";

		/** Configuration name of {@link #getScript()}. */
		String SCRIPT = "script";

		/**
		 * The name a search uses for the keyword.
		 *
		 * @see #NAME
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * TL-Script of one argument, the object, answering the value or values: an object counts by
		 * its identifier, an enumeration value by its name, anything else by its text.
		 *
		 * @see #SCRIPT
		 */
		@Name(SCRIPT)
		@Mandatory
		Expr getScript();
	}

}
