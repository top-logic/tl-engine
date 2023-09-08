/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.model.search.ui.model.misc.Multiplicity.*;

/**
 * Compare operations on object references.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public enum SingletonCompareOperation {

	/**
	 * The attribute value is equal to the object on the right side.
	 */
	EQUALS(SINGLE_VALUE),

	/**
	 * The attribute value is not equal to the right side.
	 */
	NOT_EQUALS(SINGLE_VALUE),

	/**
	 * The attribute value is contained in the literal set.
	 */
	CONTAINED_IN(COLLECTION_VALUE),

	/**
	 * The attribute value is not contained in the literal set.
	 */
	NOT_CONTAINED_IN(COLLECTION_VALUE);

	private final boolean _multiplicity;

	SingletonCompareOperation(boolean multiplicity) {
		_multiplicity = multiplicity;
	}

	/** Whether the right side of the expression represents a single value or a collection. */
	public boolean isMultiple() {
		return _multiplicity;
	}

}
