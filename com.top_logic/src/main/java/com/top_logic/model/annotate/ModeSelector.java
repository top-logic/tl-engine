/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.col.Sink;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;

/**
 * Algorithm computing a dynamic form field mode for displaying a model attribute value.
 * 
 * @see TLDynamicVisibility#getModeSelector()
 */
public interface ModeSelector {

	/**
	 * Computes the field mode.
	 *
	 * @param object
	 *        The object instance that is displayed.
	 * @param attribute
	 *        The model attribute that is represented by a form field.
	 * @param editMode
	 *        Whether the form displaying the attribute is in edit mode. Allows mode-dependent
	 *        visibility such as hiding an empty value in view mode while keeping it visible for
	 *        editing.
	 * @return The field mode of the field.
	 */
	FormVisibility getMode(TLObject object, TLStructuredTypePart attribute, boolean editMode);

	/**
	 * Reports dependencies the computed field mode depends on.
	 *
	 * @param object
	 *        The object instance that is displayed.
	 * @param attribute
	 *        The model attribute that is represented by a form field.
	 * @param trace
	 *        Callback for reporting all values, the field mode depends on.
	 * @param overlays
	 *        The overlay lookup, or <code>null</code> if not available.
	 */
	void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays);

}
