/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.AspectNaming;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * {@link ValueRef} that accesses some named model.
 * 
 * @deprecated Use {@link AspectNaming}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
@Abstract
public interface ModelPart extends ValueRef {

	/**
	 * The description of the field to access.
	 */
	ModelName getFieldName();

	/**
	 * @see #getFieldName()
	 */
	void setFieldName(ModelName fieldName);

}
