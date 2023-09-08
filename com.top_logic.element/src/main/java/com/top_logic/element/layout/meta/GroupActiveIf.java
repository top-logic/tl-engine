/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.func.GenericFunction;
import com.top_logic.layout.form.model.FieldMode;

/**
 * {@link FieldMode#ACTIVE} iff all arguments are <code>true</code>,
 * {@link FieldMode#LOCALLY_IMMUTABLE} otherwise.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GroupActiveIf extends GenericFunction<FieldMode> {
	@Override
	public FieldMode invoke(Object... args) {
		for (Object arg : args) {
			if (!(arg != null && ((Boolean) arg).booleanValue())) {
				return FieldMode.LOCALLY_IMMUTABLE;
			}
		}
		return FieldMode.ACTIVE;
	}

	@Override
	public int getArgumentCount() {
		return 0;
	}

	@Override
	public boolean hasVarArgs() {
		return true;
	}
}