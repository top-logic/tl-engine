/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import com.top_logic.layout.form.FormMember;


/**
 * Has the {@link FormMember} that label?
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FieldLabelAssertion extends FormAssertion {

	String getLabel();
	void setLabel(String label);

}
