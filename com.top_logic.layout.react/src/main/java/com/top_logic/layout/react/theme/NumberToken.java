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
 * A {@link ThemeToken} holding a unit-less number.
 */
public final class NumberToken extends ThemeToken<NumberToken.Config> {

	/**
	 * Configuration of a {@link NumberToken}.
	 */
	@TagName("number")
	public interface Config extends ThemeToken.Config<NumberToken> {

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/**
		 * The numeric value.
		 */
		@Name(VALUE)
		@Mandatory
		double getValue();

	}

	/**
	 * Creates a {@link NumberToken} from configuration.
	 */
	@CalledByReflection
	public NumberToken(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String cssValue() {
		double value = getConfig().getValue();
		if (value == Math.rint(value) && !Double.isInfinite(value)) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

}
