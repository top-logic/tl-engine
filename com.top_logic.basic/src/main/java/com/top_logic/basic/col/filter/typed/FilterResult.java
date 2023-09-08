/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.typed;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Whether a {@link TypedFilter} {@link #TRUE accepts} or {@link #FALSE rejects} an object, or is
 * {@link #INAPPLICABLE not applicable}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public enum FilterResult implements ExternallyNamed {

	/** The filter accepted the object. */
	TRUE(true),

	/** The filter did not accept the object. */
	FALSE(false),

	/** The object has the wrong type and cannot be filtered. */
	INAPPLICABLE(null);

	private final Boolean _boolean;

	FilterResult(Boolean value) {
		_boolean = value;
	}

	/**
	 * Convert the {@link Boolean} to a {@link FilterResult}.
	 */
	public static FilterResult valueOf(Boolean value) {
		if (value == null) {
			return INAPPLICABLE;
		}
		return valueOf(value.booleanValue());
	}

	/**
	 * Converts the given <code>boolean</code> to a {@link FilterResult}.
	 */
	public static FilterResult valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}

	/**
	 * Convert this {@link FilterResult} into a {@link Boolean}.
	 */
	public Boolean toBoolean() {
		return _boolean;
	}

	@Override
	public String getExternalName() {
		return name().toLowerCase();
	}

}
