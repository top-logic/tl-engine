/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.FieldMode;

/**
 * Assertion that the {@link FormMember} has the given {@link FieldMode}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FieldModeAssertion extends FormAssertion {

	/** The expected mode of the field. */
	FieldMode getMode();
	/** Setter for {@link #getMode()}. */
	void setMode(FieldMode mode);

}
