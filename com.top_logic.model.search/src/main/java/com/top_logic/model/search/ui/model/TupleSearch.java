/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import java.util.List;

import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.template.util.SpanResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLType;

/**
 * UI for creating a tuple search expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface TupleSearch extends UnionSearchPart {

	/**
	 * Definition of the tuple's coordinates.
	 */
	@ListDefault({ CoordDef.class, CoordDef.class })
	List<CoordDef> getCoords();

	/**
	 * Definition of a tuples coordinate values.
	 */
	@UseTemplate(ResourceDisplay.class)
	interface CoordDef extends NamedDefinition {

		/**
		 * @see #getExpr()
		 */
		String EXPR = "expr";

		/**
		 * Whether this {@link CoordDef coordinate} is optional or mandatory.
		 * 
		 * @see NullableSpec#MANDATORY
		 */
		NullableSpec getNullable();

		@Override
		@Mandatory
		String getName();

		/**
		 * UI for entering the expression for creating values for this {@link CoordDef coordinate}.
		 */
		@Name(EXPR)
		@ItemDefault
		@NonNullable
		InlineTypeSearch getExpr();

		@Override
		@DerivedRef({ EXPR, InlineTypeSearch.VALUE_TYPE })
		TLType getValueType();

		@Override
		@DerivedRef({ EXPR, InlineTypeSearch.VALUE_MULTIPLICITY })
		boolean getValueMultiplicity();

		/**
		 * UI part representing a coordinate expression.
		 */
		@UseTemplate(SpanResourceDisplay.class)
		interface InlineTypeSearch extends AbstractTypeSearch {
			// Pure marker interface.
		}
	}

	/**
	 * Option whether a tuple's coordinate is filled with <code>null</code>, if no matches are found
	 * for that coordinate.
	 */
	enum NullableSpec {
		/**
		 * The coordinate is mandatory. If no match is found, no tuple result is produced.
		 * 
		 * @see #NULLABLE
		 */
		MANDATORY,

		/**
		 * The coordinate is optional. If no match is found for that coordinate, it is filled with
		 * <code>null</code>.
		 * 
		 * @see #MANDATORY
		 */
		NULLABLE
	}
}
