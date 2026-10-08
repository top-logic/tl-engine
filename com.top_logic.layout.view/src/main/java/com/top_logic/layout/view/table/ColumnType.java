/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotationContainer;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * What a table column holds: the model type of its values, whether a cell holds a single value or
 * several, and where the display annotations of those values are read from.
 *
 * <p>
 * This is all a column needs to know to display, sort, filter and search its values, so a column
 * over a value no model attribute holds gets the same affordances as one over an attribute.
 * </p>
 *
 * <p>
 * A column whose values <em>are</em> held by an attribute names it as well: an attribute decides
 * more than its type does - which options a value is selected from, for instance - so a display
 * built for it stays the one a form shows.
 * </p>
 *
 * <p>
 * A descriptor is <em>unresolved</em> when it has no {@link #type()}: nothing is known about the
 * values, which happens for a column over an attribute of a row type that could not be resolved.
 * Such a column shows, sorts and filters its values by their display label, which every value has.
 * </p>
 *
 * @param type
 *        The model type of the column's values, or {@code null} when it is unresolved.
 * @param multiple
 *        Whether a cell holds a collection of values rather than a single one.
 * @param annotations
 *        Where the display annotations of the values are read from: the attribute holding them
 *        where one does, the type itself otherwise, either of them possibly overridden by the
 *        annotations a column declares, see {@link #annotatedBy(AnnotationLookup)}.
 * @param part
 *        The model attribute holding the values, or {@code null} when no attribute holds them.
 */
public record ColumnType(TLType type, boolean multiple, AnnotationLookup annotations, TLStructuredTypePart part) {

	/**
	 * The descriptor of a column nothing is known about, see {@link ColumnType}.
	 */
	public static final ColumnType UNRESOLVED =
		new ColumnType(null, false, AnnotationContainer.EMPTY, null);

	/**
	 * The descriptor of a column over the given model attribute.
	 *
	 * @param part
	 *        The attribute the column shows, or {@code null} for an {@link #UNRESOLVED} one.
	 */
	public static ColumnType of(TLStructuredTypePart part) {
		if (part == null) {
			return UNRESOLVED;
		}
		return new ColumnType(part.getType(), part.isMultiple(), part, part);
	}

	/**
	 * The descriptor of a column over values of the given type that no attribute holds.
	 *
	 * @param type
	 *        The model type of the values, or {@code null} for an {@link #UNRESOLVED} column.
	 * @param multiple
	 *        Whether a cell holds a collection of values rather than a single one.
	 */
	public static ColumnType of(TLType type, boolean multiple) {
		if (type == null) {
			return UNRESOLVED;
		}
		return new ColumnType(type, multiple, type, null);
	}

	/**
	 * Whether the type of the column's values is known, see {@link ColumnType}.
	 */
	public boolean resolved() {
		return type != null;
	}

	/**
	 * The descriptor of a column whose cells hold a collection of the values described here.
	 *
	 * <p>
	 * A column reaching its values over a multi-valued step - the values of an attribute of every
	 * object a reference points to - shows all of them in one cell, and is otherwise the column
	 * this descriptor says it is.
	 * </p>
	 */
	public ColumnType collected() {
		return multiple ? this : new ColumnType(type, true, annotations, part);
	}

	/**
	 * The descriptor of a column whose values are displayed according to the given annotations
	 * where they say something, and according to the annotations described here otherwise.
	 *
	 * <p>
	 * A column declaration states display annotations of its own - the format of a number, say -
	 * that take precedence over the ones of the attribute or type holding its values. An
	 * unresolved descriptor stays unresolved: without a type, no annotation is read.
	 * </p>
	 *
	 * @param overrides
	 *        The annotations taking precedence.
	 */
	public ColumnType annotatedBy(AnnotationLookup overrides) {
		if (!resolved()) {
			return this;
		}
		return new ColumnType(type, multiple, new LayeredAnnotations(overrides, annotations), part);
	}

	/**
	 * Annotations looked up in the {@link #overrides()} first and in the {@link #base()} where the
	 * overrides have none of the requested type.
	 *
	 * @param overrides
	 *        The annotations taking precedence.
	 * @param base
	 *        The annotations answering where the overrides say nothing.
	 */
	private record LayeredAnnotations(AnnotationLookup overrides, AnnotationLookup base) implements AnnotationLookup {

		@Override
		public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
			T result = overrides.getAnnotation(annotationType);
			return result != null ? result : base.getAnnotation(annotationType);
		}

	}

}
