/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.Collections;
import java.util.Set;

import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotationLookup;

/**
 * Abstraction of a {@link TLType} describing values at certain places in the UI (like table cells).
 * 
 * <p>
 * In the simplest case, a type context represents a {@link TLTypePart} displayed in a column. In
 * more complex cases, multiple attributes with the same name of different types are displayed in
 * the same column. The type context can also be used to define additional annotations for a
 * computed column not represented in the model.
 * </p>
 * 
 * @see ColumnInfo#getTypeContext()
 */
public interface TLTypeContext extends AnnotationLookup {

	/**
	 * The underlying type.
	 * 
	 * <p>
	 * Must only be used for model operations, not for configuring the UI based on type annotations.
	 * </p>
	 */
	TLType getType();

	/**
	 * Whether multiple values can be displayed.
	 */
	boolean isMultiple();

	/**
	 * Whether at least one value is displayed.
	 */
	boolean isMandatory();

	/**
	 * Whether this UI location represents a composition reference.
	 */
	boolean isCompositionContext();

	/**
	 * The {@link TLTypePart} that defines this context.
	 * 
	 * @return The {@link TLTypePart} that is shown at this location, or <code>null</code> if this
	 *         location is not directly derived from a {@link TLTypePart}.
	 */
	TLTypePart getTypePart();

	/**
	 * Joins this {@link ConcreteTypeContext} with another {@link ConcreteTypeContext} displayed at the same
	 * (polymorphic) location.
	 */
	default TLTypeContext join(TLTypeContext other) {
		return SumTypeContext.joinContexts(this, other);
	}

	/**
	 * If this location is defined by a sum type (one or another type), all those concrete types, a
	 * singleton set with {@link #getType()}, otherwise.
	 */
	default Set<TLType> getConcreteTypes() {
		return Collections.singleton(getType());
	}

}
