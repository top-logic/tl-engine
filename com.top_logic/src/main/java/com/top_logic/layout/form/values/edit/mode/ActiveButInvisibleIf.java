/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.mode;

import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;

/**
 * {@link DynamicMode} function that makes an active field invisible, if a certain condition is met.
 */
public class ActiveButInvisibleIf extends Function1<FieldMode, Boolean> {
	@Override
	public FieldMode apply(Boolean arg) {
		return arg != null && arg.booleanValue() ? FieldMode.INVISIBLE : FieldMode.ACTIVE;
	}
}
