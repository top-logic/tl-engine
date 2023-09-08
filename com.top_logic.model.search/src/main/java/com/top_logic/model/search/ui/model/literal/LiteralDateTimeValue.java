/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.literal;

import java.util.Date;

import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.layout.form.values.edit.editor.DateTimeEditor;

/**
 * {@link LiteralPrimitiveValue} describing a literal date with time value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(LiteralPrimitiveValue.Display.class)
public interface LiteralDateTimeValue extends LiteralPrimitiveValue {

	@Override
	@PropertyEditor(DateTimeEditor.class)
	Date getCompareValue();

}
