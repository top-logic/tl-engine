/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Algorithm computing a CSS class for a model value.
 */
public interface CssClassProvider {

	/**
	 * Computes a dynamic CSS class to assign to the given value.
	 *
	 * @param model
	 *        The context that provides the value. <code>null</code> when the context is not known.
	 * @param attribute
	 *        The attribute that has the value assigned. <code>null</code> when the value is not
	 *        directly assigned to some attribute.
	 * @param value
	 *        The value to classify.
	 * @return The CSS class to use for displaying the given value.
	 */
	String getCssClass(TLObject model, TLStructuredTypePart attribute, Object value);

}
