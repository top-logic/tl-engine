/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.layout.Control;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.form.FormField;

/**
 * An interface for {@link FormField} {@link Control}s that can write a placeholder hint, when the value is empty.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface WithPlaceHolder {

	/** The value being displayed as hint for the field, if no value has been entered yet. */
	@TemplateVariable("placeholder")
	String getPlaceHolder();

	/** @see #getPlaceHolder() */
	void setPlaceHolder(String placeHolder);

}
