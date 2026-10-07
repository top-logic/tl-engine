/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.gui.DesignTokenKind;

/**
 * A {@link ThemeToken} that aliases another token, rendered as {@code var(--other)}.
 */
public final class RefToken extends ThemeToken<RefToken.Config> {

	/**
	 * Configuration of a {@link RefToken}.
	 */
	@TagName("ref")
	public interface Config extends ThemeToken.Config<RefToken> {

		/** Configuration name for {@link #getRef()}. */
		String REF = "ref";

		/**
		 * Name of the token this token aliases.
		 */
		@Name(REF)
		@Mandatory
		String getRef();

	}

	/**
	 * Creates a {@link RefToken} from configuration.
	 */
	@CalledByReflection
	public RefToken(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String aliasedToken() {
		return getConfig().getRef();
	}

	@Override
	public String cssValue() {
		return "var(--" + aliasedToken() + ")";
	}

	/**
	 * The kind of the token this one aliases, which is not known to the token itself.
	 *
	 * @return Always <code>null</code>, the kind follows from the aliased token and is resolved
	 *         where the tokens of a theme are collected.
	 *
	 * @see UIThemeService
	 */
	@Override
	public DesignTokenKind kind() {
		return null;
	}

}
