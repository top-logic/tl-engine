/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form.typed;

import com.top_logic.model.TLObject;

/**
 * Java interface of the test type <code>TestViewTypedBuffers:TypedItem</code>.
 */
public interface TypedItem extends TLObject {

	/**
	 * Name of the attribute <code>name</code>.
	 */
	String NAME_ATTR = "name";

	/**
	 * Getter for part {@link #NAME_ATTR}.
	 */
	default String getName() {
		return (String) tValueByName(NAME_ATTR);
	}

	/**
	 * Setter for part {@link #NAME_ATTR}.
	 */
	default void setName(String newValue) {
		tUpdateByName(NAME_ATTR, newValue);
	}

}
