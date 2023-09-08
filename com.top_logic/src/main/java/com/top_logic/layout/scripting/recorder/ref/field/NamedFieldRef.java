/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.field;

import com.top_logic.layout.form.FormField;

/**
 * A reference to a {@link FormField} by the field's local name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NamedFieldRef extends FieldRef {

	/**
	 * The name of the referenced field within its parent container.
	 */
	String getFieldName();
	void setFieldName(String value);

}
