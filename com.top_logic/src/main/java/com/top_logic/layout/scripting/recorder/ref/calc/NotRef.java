/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.calc;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * A {@link ValueRef} that negates its inner boolean value.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public interface NotRef extends ValueRef {

	/**
	 * The {@link Boolean} value to negate.
	 */
	ModelName getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(ModelName value);

}
