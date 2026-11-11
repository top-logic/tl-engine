/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.model.TLObject;
import com.top_logic.model.form.OverlayLookup;

/**
 * The edit of an object, as the display of one of its attributes follows it: the overlays the
 * edited values are read from, and the announcements that the value of an attribute of the object
 * changed.
 *
 * <p>
 * Whatever the display of an attribute computes from the other values of its object - its
 * {@link AttributeModeTracker mode}, for instance - is computed over the {@link #overlays()} and
 * computed again when an attribute it read is announced to have changed.
 * </p>
 *
 * @see #of(FormControl)
 * @see RowSetEditSession#rowChanges(TLObject)
 */
public interface AttributeChanges {

	/**
	 * The overlays of the edit, which the values of edited objects are read from; {@code null}
	 * while nothing is edited.
	 */
	OverlayLookup overlays();

	/**
	 * Registers the given listener for the changes of the attributes of the edited object.
	 *
	 * <p>
	 * A notification in progress may still reach a listener that was just removed.
	 * </p>
	 *
	 * @param listener
	 *        The listener to call with the attribute whose value changed.
	 * @return The action removing the listener again.
	 */
	Runnable observe(FormControl.FieldChangeListener listener);

	/**
	 * The edit of the object of the given form: the overlays of the form and the changes of its
	 * fields.
	 *
	 * @see FormControl#getValidationModel()
	 * @see FormControl#addFieldChangeListener(FormControl.FieldChangeListener)
	 */
	static AttributeChanges of(FormControl form) {
		return new AttributeChanges() {
			@Override
			public OverlayLookup overlays() {
				return form.getValidationModel();
			}

			@Override
			public Runnable observe(FormControl.FieldChangeListener listener) {
				form.addFieldChangeListener(listener);
				return () -> form.removeFieldChangeListener(listener);
			}
		};
	}

}
