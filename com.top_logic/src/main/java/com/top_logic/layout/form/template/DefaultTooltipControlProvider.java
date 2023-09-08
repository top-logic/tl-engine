/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.control.TooltipControl;

/**
 * {@link DefaultFormFieldControlProvider} that wraps the created {@link Control} into a
 * {@link TooltipControl}.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DefaultTooltipControlProvider extends DefaultFormFieldControlProvider {

	/**
	 * Singleton {@link DefaultTooltipControlProvider} instance.
	 */
	public static final DefaultTooltipControlProvider INSTANCE = new DefaultTooltipControlProvider();

	private DefaultTooltipControlProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createControl(Object model, String style) {
		Control theControl = super.createControl(model, style);
		if (theControl == null || !(theControl instanceof AbstractFormMemberControl)) {
			return null;
		}

		return new TooltipControl((AbstractFormMemberControl) theControl);
	}

}
