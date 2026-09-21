/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.gui.DesignTokenKind;
import com.top_logic.gui.DesignTokenService;

/**
 * The design tokens of the themes of the React user interface.
 *
 * <p>
 * A page of that user interface carries the custom properties of these themes and no others, so in
 * an application with it they are the vocabulary a configuration names a design value from.
 * </p>
 *
 * <p>
 * The vocabulary is the union over all themes, since a token declared by a single theme is emitted
 * while that theme is in effect and is therefore a name a configuration may use.
 * </p>
 *
 * @implNote The names and kinds are those of {@link UITheme#getTokenKinds()}, resolved together
 *           with the token values by {@link UIThemeService}.
 */
@Label("UI theme design tokens")
@ServiceDependencies(UIThemeService.Module.class)
public class UIThemeDesignTokens extends DesignTokenService {

	/**
	 * Creates a {@link UIThemeDesignTokens} from the given configuration.
	 *
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link UIThemeDesignTokens}.
	 */
	public UIThemeDesignTokens(InstantiationContext context, ManagedClass.ServiceConfiguration<?> config) {
		super(context, config);
	}

	@Override
	public Collection<String> getTokenNames(DesignTokenKind kind) {
		Set<String> result = new LinkedHashSet<>();
		for (UITheme theme : UIThemeService.getInstance().getThemes()) {
			for (Map.Entry<String, DesignTokenKind> token : theme.getTokenKinds().entrySet()) {
				if (token.getValue() == kind) {
					result.add(token.getKey());
				}
			}
		}
		return result;
	}

}
