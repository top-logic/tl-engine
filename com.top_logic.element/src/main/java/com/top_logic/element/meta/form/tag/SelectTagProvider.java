/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DropDownControl;

/**
 * {@link DisplayProvider} creating select tag.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectTagProvider implements DisplayProvider {

	/**
	 * Singleton {@link SelectTagProvider} instance.
	 */
	public static final SelectTagProvider INSTANCE = new SelectTagProvider();

	private SelectTagProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		return new DropDownControl((FormField) member);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		return new DropDownControl((FormField) member);
	}

}
