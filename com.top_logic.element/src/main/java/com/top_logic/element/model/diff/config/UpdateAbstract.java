/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Update of the <code>abstract</code> state of a {@link TLStructuredTypePart}.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("update-abstract")
@Label("Update abstract")
public interface UpdateAbstract extends PartUpdate {

	/**
	 * The new {@link TLStructuredTypePart#isAbstract() abstract} state.
	 */
	boolean isAbstract();

	/** @see #isAbstract() */
	void setAbstract(boolean value);

}
