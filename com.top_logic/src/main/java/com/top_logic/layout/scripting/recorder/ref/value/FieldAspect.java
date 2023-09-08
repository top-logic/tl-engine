/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Abstract {@link ValueRef} resolving to some property of a {@link FormField}.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
@Abstract
public interface FieldAspect extends ValueRef {

	/**
	 * Field to access.
	 */
	ModelName getField();

	/**
	 * @see #getField()
	 */
	void setField(ModelName value);

}
