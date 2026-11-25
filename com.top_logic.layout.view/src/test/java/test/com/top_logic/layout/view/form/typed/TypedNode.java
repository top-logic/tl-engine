/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form.typed;

import com.top_logic.element.structured.StructuredElement;

/**
 * Java interface of the test type <code>TestViewTypedBuffers:TypedNode</code> below
 * <code>tl.element:StructuredElement</code>.
 */
public interface TypedNode extends StructuredElement {

	/**
	 * Name of the attribute <code>label</code>.
	 */
	String LABEL_ATTR = "label";

	/**
	 * Getter for part {@link #LABEL_ATTR}.
	 */
	default String getLabel() {
		return (String) tValueByName(LABEL_ATTR);
	}

	/**
	 * Setter for part {@link #LABEL_ATTR}.
	 */
	default void setLabel(String newValue) {
		tUpdateByName(LABEL_ATTR, newValue);
	}

}
