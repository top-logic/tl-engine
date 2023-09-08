/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.func.GenericFunction;
import com.top_logic.layout.form.model.FieldMode;

/**
 * {@link GenericFunction} switching between two modes based on a condition (computed as
 * and-combination of potentially multiple inputs).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ModeSwitch extends GenericFunction<FieldMode> {
	@Override
	public FieldMode invoke(Object... args) {
		for (Object arg : args) {
			if (!(arg != null && ((Boolean) arg).booleanValue())) {
				return falseMode();
			}
		}
		return trueMode();
	}

	/**
	 * The {@link FieldMode} if the condition is <code>true</code>.
	 */
	protected abstract FieldMode trueMode();

	/**
	 * The {@link FieldMode} if the condition is <code>false</code>.
	 */
	protected abstract FieldMode falseMode();

	@Override
	public int getArgumentCount() {
		return 0;
	}

	@Override
	public boolean hasVarArgs() {
		return true;
	}
}
