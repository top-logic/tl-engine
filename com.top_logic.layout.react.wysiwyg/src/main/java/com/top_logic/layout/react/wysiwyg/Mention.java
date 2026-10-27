/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * A way of referring to objects in an HTML text by typing a trigger character: the editor offers
 * the objects matching the text typed after the trigger, and inserts a
 * {@link TLObjectLinkUtil#TL_OBJECT link to the object} picked.
 *
 * <p>
 * The objects offered are computed by a TL-Script function, see {@link Config#getOptions()}.
 * </p>
 */
public final class Mention {

	/**
	 * Configuration of a {@link Mention}.
	 */
	public interface Config extends ConfigurationItem {

		/** Configuration name for {@link #getTrigger()}. */
		String TRIGGER = "trigger";

		/** Configuration name for {@link #getOptions()}. */
		String OPTIONS = "options";

		/** Configuration name for {@link #getPresentation()}. */
		String PRESENTATION = "presentation";

		/** The {@link #getPresentation()} of a mention that states none. */
		String DEFAULT_PRESENTATION = "chip";

		/**
		 * The character starting the mention when typed, e.g. <code>@</code> or <code>#</code>.
		 *
		 * <p>
		 * A single character, which no other mention of the same editor uses.
		 * </p>
		 */
		@Name(TRIGGER)
		@Mandatory
		String getTrigger();

		/**
		 * Function computing the objects offered while the mention is typed.
		 *
		 * <p>
		 * The function has the signature {@code self -> query -> objects}: {@code self} is the
		 * edited object (empty for a field editing no attribute of an object), {@code query} the
		 * text typed after the trigger character. The result is a single object or a collection
		 * of objects, offered in their order by their labels. The function decides which objects
		 * match the query.
		 * </p>
		 */
		@Name(OPTIONS)
		@Mandatory
		Expr getOptions();

		/**
		 * How the editor shows a reference to an object picked.
		 */
		@Name(PRESENTATION)
		@FormattedDefault(DEFAULT_PRESENTATION)
		MentionPresentation getPresentation();

	}

	private final String _trigger;

	private final MentionPresentation _presentation;

	private final QueryExecutor _options;

	/**
	 * Creates a {@link Mention} from its configuration.
	 */
	public Mention(Config config) {
		this(config.getTrigger(), config.getPresentation(), QueryExecutor.compile(config.getOptions()));
	}

	/**
	 * Creates a {@link Mention}.
	 *
	 * @param trigger
	 *        See {@link #getTrigger()}.
	 * @param presentation
	 *        See {@link #getPresentation()}.
	 * @param options
	 *        The compiled function computing the objects, see {@link Config#getOptions()}.
	 */
	public Mention(String trigger, MentionPresentation presentation, QueryExecutor options) {
		_trigger = trigger;
		_presentation = presentation;
		_options = options;
	}

	/**
	 * The character starting the mention, see {@link Config#getTrigger()}.
	 */
	public String getTrigger() {
		return _trigger;
	}

	/**
	 * How the editor shows a reference to an object picked.
	 */
	public MentionPresentation getPresentation() {
		return _presentation;
	}

	/**
	 * The objects offered for the given query, in the order the function computes them.
	 *
	 * <p>
	 * A value of the result that is no {@link TLObject} cannot be linked to and is skipped and
	 * logged, so that it does not cost the user the other objects.
	 * </p>
	 *
	 * @param self
	 *        The edited object, {@code null} for a field editing no attribute of an object.
	 * @param query
	 *        The text typed after the trigger character.
	 */
	public List<TLObject> options(Object self, String query) {
		List<?> values = SearchExpression.asList(_options.execute(self, query));
		List<TLObject> result = new ArrayList<>(values.size());
		for (Object value : values) {
			if (value instanceof TLObject object) {
				result.add(object);
			} else if (value != null) {
				Logger.warn("Skipping a value offered for the mention '" + _trigger + query
					+ "' that is no object to link to: " + value, Mention.class);
			}
		}
		return result;
	}

}
