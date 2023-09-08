/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.form.model.FormGroup;

/**
 * {@link Accessor} that looks up {@link FormMember}s of {@link FormGroup}s
 * with the given property name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormGroupAccessor extends ReadOnlyAccessor<FormGroup> {

	/**
	 * Singleton {@link FormGroupAccessor} instance.
	 */
	public static final FormGroupAccessor INSTANCE = new FormGroupAccessor();
	
	private FormGroupAccessor() {
		// Singleton constructor
	}
	
	@Override
	public Object getValue(FormGroup object, String property) {
		FormGroup formGroup = object;
		if (formGroup.hasMember(property)) {
			return formGroup.getMember(property);
		} else {
			return null;
		}
	}

}
