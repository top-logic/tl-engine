/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.table.filter.ComparisonOperator;

/**
 * What a declared filter criterion selects in one column, written in the form the filter of that
 * column works in.
 *
 * <p>
 * Each kind of filter has its own kind of criterion, and a criterion is written as the one that
 * fits the column it addresses: a text pattern with its matching options for a text filter, a
 * comparison for a range filter, a selection for a filter offering options, and the accepted truth
 * values for a boolean filter. A criterion of another kind than the column's filter is a
 * configuration error, and so the preset it belongs to is not offered.
 * </p>
 *
 * <p>
 * The values a criterion selects by are TL-Script expressions, evaluated with the input values of
 * the table, so that a criterion can select by what is displayed elsewhere - the object selected in
 * another table, the current user - while everything that decides the <em>shape</em> of the
 * criterion (which comparison, whether the pattern is a regular expression) is fixed by the
 * declaration.
 * </p>
 */
@Abstract
public interface FilterStateConfig extends ConfigurationItem {

	/**
	 * A criterion for a column filtered by a text pattern.
	 *
	 * <p>
	 * The matching options are the ones the filter of the column offers the user, and they mean the
	 * same here: a pattern matches a part of the cell text, case-insensitively, unless declared
	 * otherwise.
	 * </p>
	 */
	@TagName(TextConfig.TAG_NAME)
	interface TextConfig extends FilterStateConfig {

		/** Configuration tag of a {@link TextConfig}. */
		String TAG_NAME = "text";

		/** Configuration name for {@link #getPattern()}. */
		String PATTERN = "pattern";

		/** Configuration name for {@link #getCaseSensitive()}. */
		String CASE_SENSITIVE = "case-sensitive";

		/** Configuration name for {@link #getRegexp()}. */
		String REGEXP = "regexp";

		/** Configuration name for {@link #getWholeField()}. */
		String WHOLE_FIELD = "whole-field";

		/**
		 * TL-Script expression computing the pattern to match.
		 *
		 * <p>
		 * The text the expression yields is the pattern; an expression yielding nothing filters
		 * nothing, so that a criterion matching what is selected elsewhere leaves the column
		 * unfiltered while nothing is selected.
		 * </p>
		 */
		@Name(PATTERN)
		@Mandatory
		@NonNullable
		Expr getPattern();

		/**
		 * Whether upper and lower case have to match, too.
		 */
		@Name(CASE_SENSITIVE)
		boolean getCaseSensitive();

		/**
		 * Whether the pattern is a regular expression rather than a piece of text to be found.
		 */
		@Name(REGEXP)
		boolean getRegexp();

		/**
		 * Whether the whole cell text has to match, rather than a part of it.
		 */
		@Name(WHOLE_FIELD)
		boolean getWholeField();
	}

	/**
	 * A criterion for a column filtered by comparing its values.
	 */
	@TagName(RangeConfig.TAG_NAME)
	interface RangeConfig extends FilterStateConfig {

		/** Configuration tag of a {@link RangeConfig}. */
		String TAG_NAME = "range";

		/** Configuration name for {@link #getOperator()}. */
		String OPERATOR = "operator";

		/** Configuration name for {@link #getPrimary()}. */
		String PRIMARY = "primary";

		/** Configuration name for {@link #getSecondary()}. */
		String SECONDARY = "secondary";

		/**
		 * How the cell value is compared to the declared bounds.
		 */
		@Name(OPERATOR)
		@Mandatory
		ComparisonOperator getOperator();

		/**
		 * TL-Script expression computing the value the cell value is compared to - the lower bound
		 * of a comparison against a range.
		 *
		 * <p>
		 * An expression yielding nothing filters nothing.
		 * </p>
		 */
		@Name(PRIMARY)
		@Mandatory
		@NonNullable
		Expr getPrimary();

		/**
		 * TL-Script expression computing the upper bound of a comparison against a range, which is
		 * the only comparison using a second bound.
		 */
		@Name(SECONDARY)
		@Nullable
		Expr getSecondary();
	}

	/**
	 * A criterion for a column filtered by selecting among its values.
	 */
	@TagName(OptionsConfig.TAG_NAME)
	interface OptionsConfig extends FilterStateConfig {

		/** Configuration tag of an {@link OptionsConfig}. */
		String TAG_NAME = "options";

		/** Configuration name for {@link #getSelected()}. */
		String SELECTED = "selected";

		/**
		 * TL-Script expression computing the value, or the collection of values, to select.
		 *
		 * <p>
		 * Each computed value has to be one the column offers as an option; a value it does not
		 * offer is a configuration error, and the preset is then not offered - a selection missing
		 * one of its values matches other rows than the declaration says. A value that is not there
		 * selects nothing, so a criterion selecting what is displayed elsewhere leaves the column
		 * unfiltered while nothing is selected there.
		 * </p>
		 */
		@Name(SELECTED)
		@Mandatory
		@NonNullable
		Expr getSelected();
	}

	/**
	 * A criterion for a column filtered by a truth value.
	 */
	@TagName(BooleanConfig.TAG_NAME)
	interface BooleanConfig extends FilterStateConfig {

		/** Configuration tag of a {@link BooleanConfig}. */
		String TAG_NAME = "boolean";

		/** Configuration name for {@link #getAccept()}. */
		String ACCEPT = "accept";

		/**
		 * TL-Script expression computing which truth values are accepted: one of them, or a
		 * collection of several.
		 *
		 * <p>
		 * Nothing stands for the cells without a value, which are accepted only by a column that
		 * has such cells; everything else is read as a truth value the way TL-Script reads one. A
		 * criterion accepting all the values a column has filters nothing.
		 * </p>
		 */
		@Name(ACCEPT)
		@Mandatory
		@NonNullable
		Expr getAccept();
	}

}
