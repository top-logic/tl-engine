/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.ResKey;

/**
 * {@link Function1}, that generates a {@link ResKey} out of a given {@link Class}.
 * 
 * @see ResKey#forClass(Class)
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ResKeyFromClass extends Function1<ResKey, Class<?>> {

	@Override
	public ResKey apply(Class<?> arg) {
		if (arg == null) {
			return ResKey.NONE;
		}
		return ResKey.forClass(arg);
	}

}