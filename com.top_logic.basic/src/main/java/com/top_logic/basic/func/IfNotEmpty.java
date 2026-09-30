/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

import com.top_logic.basic.util.Utils;

/**
 * Whether the argument is not empty.
 * 
 * @see IfEmpty
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IfNotEmpty extends Function1<Boolean, Object> {

	@Override
	public Boolean apply(Object arg) {
		return !Utils.isEmpty(arg);
	}

}