/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.typed;

import com.top_logic.model.TLObject;

/**
 * Java interface of the test type <code>testTypedObjectClasses:Coded</code>.
 */
public interface Coded extends TLObject {

	/**
	 * Name of the attribute <code>code</code>.
	 */
	String CODE_ATTR = "code";

	/**
	 * Getter for part {@link #CODE_ATTR}.
	 */
	default String getCode() {
		return (String) tValueByName(CODE_ATTR);
	}

	/**
	 * Setter for part {@link #CODE_ATTR}.
	 */
	default void setCode(String newValue) {
		tUpdateByName(CODE_ATTR, newValue);
	}

}
