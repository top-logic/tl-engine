/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.FieldValidity;

/**
 * Assertion that the {@link FormField} has the given {@link FieldValidity}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FieldValidityAssertion extends FieldAssertion {

	/** The expected validity state of the field. */
	FieldValidity getValidity();
	/** Setter for {@link #getValidity()}. */
	void setValidity(FieldValidity validity);

}
