/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.expressions;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * Conversion of a list to it's only element (or <code>null</code>).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface SingletonValue extends ValueRef {

	/**
	 * The collection value to take the single element from.
	 */
	ModelName getCollection();

}
