/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * What this application knows about the identifiers a model passes to a tool.
 *
 * <p>
 * {@link TidDiagnostics} recognises the mistakes that every application sees - a uuid, a printed
 * object, an unknown type, an id of the wrong shape - by itself. The one class it cannot recognise
 * is the identifier an application's own users speak in: an order number, a ticket code, a part
 * number. Which string is such a code, and which tool turns it into an object id, is knowledge only
 * the application has, so it declares it here.
 * </p>
 *
 * @see ObjectIdSettings
 * @see TidDiagnostics#explain(String, TidDiagnostics.Types)
 */
@TagName("object-ids")
@DisplayOrder({
	ObjectIdConfig.HINTS,
})
public interface ObjectIdConfig extends ConfigPart {

	/** Configuration property name for {@link #getHints()}. */
	String HINTS = "hints";

	/**
	 * The application's own explanations, tried before the built-in ones.
	 *
	 * <p>
	 * In declaration order; the first {@link HintConfig#getPattern() pattern} that matches decides
	 * the message, and nothing after it is tried. The list composes additively across configuration
	 * files, so an application adds its hints without restating the ones another module declared.
	 * </p>
	 *
	 * @see #HINTS
	 */
	@Name(HINTS)
	@Label("Hints")
	@EntryTag("hint")
	List<HintConfig> getHints();

	/**
	 * One thing this application can say about a string that is not an object id.
	 *
	 * <p>
	 * The sentence reaches a language model, not a person, and it is worth writing as an
	 * instruction rather than as a complaint: what the string is, and which tool turns it into an
	 * id.
	 * </p>
	 */
	interface HintConfig extends ConfigurationItem {

		/** Configuration property name for {@link #getPattern()}. */
		String PATTERN = "pattern";

		/** Configuration property name for {@link #getExplanation()}. */
		String EXPLANATION = "explanation";

		/**
		 * The strings this hint is about, as a regular expression.
		 *
		 * <p>
		 * Matched against the <em>whole</em> identifier, so {@code [A-Z]+-[0-9]+} is the code
		 * {@code LEAD-014} and nothing that merely contains it. A pattern that does not compile is
		 * logged once and then matches nothing: a typo in a hint must not break the resolver it is
		 * a hint for.
		 * </p>
		 *
		 * @see #PATTERN
		 */
		@Name(PATTERN)
		@Label("Pattern")
		@Mandatory
		String getPattern();

		/**
		 * What to tell the model, continuing the sentence that begins with the quoted identifier.
		 *
		 * <p>
		 * The framework writes the identifier and quotes it, so this text starts where that leaves
		 * off: {@code "is a record code. Search for it with sales__search and use the id from the
		 * answer."} becomes {@code "LEAD-014" is a record code. Search for it with sales__search
		 * and use the id from the answer.}
		 * </p>
		 *
		 * @see #EXPLANATION
		 */
		@Name(EXPLANATION)
		@Label("Explanation")
		@Mandatory
		String getExplanation();
	}
}
