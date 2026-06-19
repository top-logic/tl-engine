/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;

/**
 * UI-neutral description of the inputs a column filter needs, so a UI tier can render a
 * filter editor without the model tier knowing any control or form types.
 *
 * <p>
 * The descriptor says <em>what kind</em> of inputs are required; the concrete user choices
 * live in a {@link FilterState}. The React tier renders each variant by building
 * {@code FieldModel}s that the existing React form controls bind to.
 * </p>
 */
public sealed interface FilterInput {

	/**
	 * A free-text pattern filter (optionally case-sensitive / regexp / whole-field; those
	 * flags live in the {@link FilterState}).
	 */
	record Text() implements FilterInput {
		// Descriptor; no payload.
	}

	/**
	 * A comparison/range filter: an operator plus one or two bound values (carried by the
	 * {@link FilterState}).
	 */
	record Range() implements FilterInput {
		// Descriptor; no payload.
	}

	/**
	 * A multi-select filter over a fixed set of {@link Option}s, optionally annotated with
	 * {@link MatchCounts facet counts}.
	 *
	 * @param values
	 *        The selectable options.
	 */
	record Options(List<Option> values) implements FilterInput {
		/**
		 * Creates an {@link Options} descriptor with a defensive, immutable copy.
		 */
		public Options {
			values = List.copyOf(values);
		}
	}

	/**
	 * A tri-state boolean filter ({@code true} / {@code false} / no-value).
	 */
	record Bool() implements FilterInput {
		// Descriptor; no payload.
	}

	/**
	 * A filter whose editor is a whole form, supplied by the UI tier (not derived from a simple
	 * input shape). The model tier carries no form details; the view layer registers a custom
	 * filter UI for the column (e.g. a model-class-defined, script-evaluated filter).
	 */
	record Form() implements FilterInput {
		// Descriptor; the UI is provided out-of-band by the view tier.
	}

}
