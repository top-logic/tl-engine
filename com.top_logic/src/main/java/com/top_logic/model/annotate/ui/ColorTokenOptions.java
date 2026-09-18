/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.gui.DesignTokenKind;
import com.top_logic.gui.DesignTokenService;

/**
 * The color design tokens the application offers to choose a {@link ColorSpec#getToken()} from.
 *
 * <p>
 * The options are the {@link DesignTokenKind#COLOR} names of the token vocabulary the application
 * emits, sorted by name. Tokens of another kind are left out, since a color annotation displays
 * nothing with them.
 * </p>
 *
 * <p>
 * A token the application does not emit stays the value of the property: it is kept alongside the
 * options and reported by {@link ColorTokenConstraint}. An application declaring no color
 * vocabulary offers nothing to choose from.
 * </p>
 *
 * @see ColorTokenConstraint
 */
public class ColorTokenOptions extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		List<String> tokens = new ArrayList<>(DesignTokenService.tokenNames(DesignTokenKind.COLOR));
		Collections.sort(tokens);
		return tokens;
	}

}
