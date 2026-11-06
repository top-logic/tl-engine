/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import com.top_logic.layout.form.FormMember;


/**
 * Assertion that the {@link FormMember} has the given label.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FieldLabelAssertion extends FormAssertion {

	/** The expected label of the field. */
	String getLabel();
	/** Setter for {@link #getLabel()}. */
	void setLabel(String label);

}
