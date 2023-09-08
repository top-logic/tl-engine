/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * Whether the argument is <code>true</code>.
 */
public class IfTrue extends Function1<Boolean, Boolean> {
	@Override
	public Boolean apply(Boolean arg) {
		return arg != null && arg.booleanValue();
	}
}