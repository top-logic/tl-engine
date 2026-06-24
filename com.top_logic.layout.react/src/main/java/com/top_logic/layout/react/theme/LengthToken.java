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
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;

/**
 * A {@link ThemeToken} holding a CSS length, e.g. {@code 0.5rem} or {@code 12px}.
 */
public final class LengthToken extends ThemeToken<LengthToken.Config> {

	/**
	 * Configuration of a {@link LengthToken}.
	 */
	@TagName("length")
	public interface Config extends ThemeToken.Config<LengthToken> {

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** A number followed by a CSS length unit, or {@code 0}. */
		String PATTERN =
			"(?:0|[+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:px|rem|em|%|vh|vw|vmin|vmax|pt|pc|cm|mm|in|ch|ex))";

		/**
		 * The CSS length value.
		 */
		@Name(VALUE)
		@Mandatory
		@RegexpConstraint(PATTERN)
		String getValue();

	}

	/**
	 * Creates a {@link LengthToken} from configuration.
	 */
	@CalledByReflection
	public LengthToken(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String cssValue() {
		return getConfig().getValue();
	}

}
