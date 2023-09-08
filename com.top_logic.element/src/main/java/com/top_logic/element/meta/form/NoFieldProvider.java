/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.layout.form.FormMember;

/**
 * {@link FieldProvider} that constantly return <code>null</code>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NoFieldProvider extends AbstractFieldProvider {

	/** Singleton {@link NoFieldProvider} instance. */
	public static final NoFieldProvider INSTANCE = new NoFieldProvider();

	private NoFieldProvider() {
		// singleton instance
	}

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		return null;
	}

}

