/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.FieldValidity;

/**
 * Has the {@link FormField} that {@link FieldValidity}?
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FieldValidityAssertion extends FieldAssertion {

	FieldValidity getValidity();
	void setValidity(FieldValidity validity);

}
