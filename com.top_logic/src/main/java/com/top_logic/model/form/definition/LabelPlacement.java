/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.form.ReactiveFormCSS;

/**
 * Decision about where to place labels in forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 * 
 * @deprecated Use {@link LabelPosition}
 */
@Deprecated
public enum LabelPlacement {
	/**
	 * The label is rendered in a column before the input and wrapped into a separate line, if there
	 * is not enough space.
	 * 
	 * @deprecated Use {@link LabelPosition#DEFAULT}
	 */
	@Deprecated
	DEFAULT,

	/**
	 * Label is rendered above its input
	 * 
	 * @deprecated Use {@link LabelPosition#ABOVE}
	 */
	@Deprecated
	ABOVE,

	/**
	 * There is only a label cell that also displays the input.
	 * 
	 * <p>
	 * This is useful for very short input elements such as checkboxes.
	 * </p>
	 * 
	 * @deprecated Use {@link LabelPosition#INLINE}
	 */
	@Deprecated
	INLINE;

	/**
	 * CSS class representing this {@link LabelPlacement}.
	 * 
	 * @return May be <code>null</code>.
	 */
	public String cssClass() {
		switch (this) {
			case ABOVE:
				return ReactiveFormCSS.RF_LABEL_ABOVE;
			case INLINE:
				return ReactiveFormCSS.RF_LABEL_INLINE;
			default:
				return null;
		}
	}
}
