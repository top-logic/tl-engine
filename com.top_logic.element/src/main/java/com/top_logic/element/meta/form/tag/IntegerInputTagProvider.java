/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.IntegerInputControl;

/**
 * {@link DisplayProvider} creating an integer field with plus and minus buttons after the input.
 */
public class IntegerInputTagProvider implements DisplayProvider {

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		return IntegerInputControl.Provider.INSTANCE.createControl(member);
	}

}
