/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Update of the <code>mandatory</code> state of a {@link TLStructuredTypePart}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("update-mandatory")
public interface UpdateMandatory extends PartUpdate {

	/**
	 * The new {@link TLStructuredTypePart#isMandatory() mandatory} state.
	 */
	boolean isMandatory();

	/** @see #isMandatory() */
	void setMandatory(boolean value);

}
