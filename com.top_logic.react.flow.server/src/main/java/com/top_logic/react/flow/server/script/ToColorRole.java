/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.script;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.operations.ValueConverter;
import com.top_logic.util.error.TopLogicException;

/**
 * Converts a color role given in TL-Script to the {@link ValueColor#getExternalName() external
 * name} of a {@link ValueColor}.
 *
 * <p>
 * Accepts a {@link ValueColor} or its external name ({@code neutral}, {@code error},
 * {@code category-3}, ...). A missing value stays {@code null}, so that a parameter without
 * argument means "no role given", unlike a parameter of the enumeration type itself, which
 * defaults to its first constant.
 * </p>
 */
public class ToColorRole implements ValueConverter {

	@Override
	public Object fromScript(Object scriptValue) {
		if (scriptValue == null) {
			return null;
		}
		if (scriptValue instanceof ValueColor role) {
			return role.getExternalName();
		}
		String name = SearchExpression.asString(scriptValue);
		if (name.isEmpty()) {
			return null;
		}
		ValueColor role = ValueColor.byExternalName(name);
		if (role == null) {
			String roles = Stream.of(ValueColor.values()).map(ValueColor::getExternalName)
				.collect(Collectors.joining(", "));
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_COLOR_ROLE__VALUE_ROLES.fill(name, roles));
		}
		return role.getExternalName();
	}

}
