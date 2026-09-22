/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.gui.DesignTokenKind;
import com.top_logic.gui.DesignTokenService;

/**
 * {@link ValueConstraint} checking that a color design token is one the user interface emits.
 *
 * <p>
 * A token the user interface does not emit resolves to nothing, so the value annotated with it is
 * displayed without its color. The constraint names such a token where it is written.
 * </p>
 *
 * <p>
 * An application declaring no token vocabulary has no names to check against. The constraint is
 * then silent and accepts every token.
 * </p>
 *
 * @see ColorSpec#getToken()
 * @see ColorTokenOptions
 */
public class ColorTokenConstraint extends ValueConstraint<String> {

	/** Separator between the known token names listed in the problem description. */
	private static final String NAME_SEPARATOR = ", ";

	/**
	 * Creates a {@link ColorTokenConstraint}.
	 */
	public ColorTokenConstraint() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> propertyModel) {
		String value = propertyModel.getValue();
		if (StringServices.isEmpty(value)) {
			return;
		}
		Collection<String> tokens = DesignTokenService.tokenNames(DesignTokenKind.COLOR);
		if (tokens.isEmpty() || tokens.contains(value)) {
			return;
		}
		propertyModel.setProblemDescription(
			I18NConstants.WARNING_NO_SUCH_COLOR_TOKEN__TOKEN_KNOWN.fill(value, knownNames(tokens)));
	}

	private static String knownNames(Collection<String> tokens) {
		List<String> sorted = new ArrayList<>(tokens);
		Collections.sort(sorted);
		return StringServices.join(sorted, NAME_SEPARATOR);
	}

}
