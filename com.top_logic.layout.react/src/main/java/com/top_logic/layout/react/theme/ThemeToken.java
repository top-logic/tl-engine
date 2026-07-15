/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * A design token: a named value rendered to a CSS custom property.
 *
 * <p>
 * Each kind of value (color, length, number, text, reference) is a {@link ThemeToken} subclass with
 * its own typed {@link Config}, so the configuration carries the proper type and the editor and
 * validation follow from it. Kinds are pluggable: add one by adding a subclass and tagging its
 * configuration.
 * </p>
 */
public abstract class ThemeToken<C extends ThemeToken.Config<?>> extends AbstractConfiguredInstance<C> {

	/**
	 * Configuration of a {@link ThemeToken}.
	 */
	@Abstract
	public interface Config<I extends ThemeToken<?>> extends PolymorphicConfiguration<I>, NamedConfigMandatory {

		/** Configuration name for {@link #getGroup()}. */
		String GROUP = "group";

		/**
		 * Optional grouping label for related tokens.
		 */
		@Name(GROUP)
		String getGroup();

	}

	/**
	 * Creates a {@link ThemeToken} from configuration.
	 *
	 * @param context
	 *        The instantiation context.
	 * @param config
	 *        The token configuration.
	 */
	@CalledByReflection
	public ThemeToken(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The token name, used as the CSS custom-property name without the leading {@code --}.
	 */
	public String getName() {
		return getConfig().getName();
	}

	/**
	 * The CSS value emitted for this token.
	 */
	public abstract String cssValue();

}
