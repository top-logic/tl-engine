/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;

/**
 * The {@link ToggleVisibilityListener} toggles the visibility of the given
 * member.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToggleVisibilityListener implements ValueListener {

	private final FormMember	member;

	public ToggleVisibilityListener(FormMember aMember) {
		member = aMember;

	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		member.setVisible(!member.isVisible());
	}

}
