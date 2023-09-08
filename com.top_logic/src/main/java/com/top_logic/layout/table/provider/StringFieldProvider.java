/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.FieldProvider;

/**
 * {@link FieldProvider} creating {@link FormFactory#newStringField(String, Object, boolean)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringFieldProvider extends AbstractFieldProvider {

	/**
	 * Singleton {@link StringFieldProvider} instance.
	 */
	public static final StringFieldProvider INSTANCE = new StringFieldProvider();

	@Override
	public FormMember createField(Object model, Accessor accessor, String property) {
		Object value = accessor.getValue(model, property);
		return FormFactory.newStringField(getFieldName(model, accessor, property), value, false);
	}

}
