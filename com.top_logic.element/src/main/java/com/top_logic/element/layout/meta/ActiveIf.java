/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.layout.form.model.FieldMode;

/**
 * {@link FieldMode#ACTIVE} iff all arguments are <code>true</code>, {@link FieldMode#IMMUTABLE}
 * otherwise.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ActiveIf extends ModeSwitch {

	@Override
	protected FieldMode trueMode() {
		return FieldMode.ACTIVE;
	}

	@Override
	protected FieldMode falseMode() {
		return FieldMode.IMMUTABLE;
	}

}