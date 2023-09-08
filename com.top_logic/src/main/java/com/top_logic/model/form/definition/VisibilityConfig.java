/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration aspect providing the {@link FormVisibility} of an input element in a form.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface VisibilityConfig extends ConfigurationItem {

	/** Configuration option of {@link #getVisibility()}. */
	String VISIBILITY = "visibility";

	/**
	 * How the columns (content) {@link FormVisibility}.
	 */
	@Name(VISIBILITY)
	FormVisibility getVisibility();

	/**
	 * @see #getVisibility()
	 */
	void setVisibility(FormVisibility value);

}


