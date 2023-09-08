/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.layout.form.model.FieldMode;

/**
 * {@link ModeSwitch} hiding an otherwise {@link FieldMode#ACTIVE} field if condition is
 * <code>true</code>.
 * 
 * @see HideActiveIfNot
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HideActiveIf extends ModeSwitch {

	@Override
	protected FieldMode trueMode() {
		return FieldMode.INVISIBLE;
	}

	@Override
	protected FieldMode falseMode() {
		return FieldMode.ACTIVE;
	}

}
