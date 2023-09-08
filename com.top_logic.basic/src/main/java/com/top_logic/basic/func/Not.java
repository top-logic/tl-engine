/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;


/**
 * {@link GenericFunction} computing a boolean <code>not</code> of a value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Not extends Function1<Boolean, Object> {

	@Override
	public Boolean apply(Object arg1) {
		return !Boolean.valueOf(And.toBoolean(arg1));
	}

}
