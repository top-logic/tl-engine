/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.recorder.ref.value.form.FieldRawValueNaming;
import com.top_logic.layout.scripting.recorder.ref.value.form.FieldValueNaming;

/**
 * {@link ValueRef} that delivers the current value of a {@link FormField}.
 * 
 * @deprecated Use {@link FieldValueNaming}, or {@link FieldRawValueNaming}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface CurrentFieldValue extends ModelPart {

	/**
	 * Is the {@link FormField#getRawValue() raw value} of the {@link FormField} meant, oder the
	 * {@link FormField#getValue() "normal" value?}
	 */
	boolean isRawValue();

	/** @see #isRawValue() */
	void setRawValue(boolean isRawValue);

}
