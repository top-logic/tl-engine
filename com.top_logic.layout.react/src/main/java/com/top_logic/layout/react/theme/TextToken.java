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

/**
 * A {@link ThemeToken} holding a raw CSS value, e.g. a font-family list, a shadow or a function such
 * as {@code rgba(...)}.
 */
public final class TextToken extends ThemeToken<TextToken.Config> {

	/**
	 * Configuration of a {@link TextToken}.
	 */
	@TagName("text")
	public interface Config extends ThemeToken.Config<TextToken> {

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/**
		 * The raw CSS value.
		 */
		@Name(VALUE)
		@Mandatory
		String getValue();

	}

	/**
	 * Creates a {@link TextToken} from configuration.
	 */
	@CalledByReflection
	public TextToken(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String cssValue() {
		return getConfig().getValue();
	}

}
