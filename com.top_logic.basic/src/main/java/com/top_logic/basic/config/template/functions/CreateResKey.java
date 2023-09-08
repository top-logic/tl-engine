/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template.functions;

import com.top_logic.basic.func.GenericFunction;
import com.top_logic.basic.util.ResKey;

/**
 * {@link GenericFunction} creating a {@link ResKey} from a {@link CharSequence} and optional
 * arguments.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateResKey extends GenericFunction<ResKey> {

	@Override
	public ResKey invoke(Object... args) {
		ResKey base = ResKey.internalJsp(args[0].toString());
		if (args.length > 1) {
			Object[] params = new Object[args.length - 1];
			System.arraycopy(args, 1, params, 0, params.length);
			return ResKey.message(base, params);
		} else {
			return base;
		}
	}

	@Override
	public int getArgumentCount() {
		return 1;
	}

	@Override
	public boolean hasVarArgs() {
		return true;
	}

}
