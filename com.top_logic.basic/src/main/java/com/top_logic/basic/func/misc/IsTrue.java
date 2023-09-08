/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func.misc;

import com.top_logic.basic.func.Function1;

/**
 * {@link Function1} deciding about its argument being <code>true</code>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IsTrue extends Function1<Boolean, Boolean> {

	@Override
	public Boolean apply(Boolean arg) {
		return arg != null && arg.booleanValue();
	}

}
