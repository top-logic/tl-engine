/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import java.awt.Color;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.format.ColorConfigFormat;

/**
 * A {@link ThemeToken} holding a color, rendered as a CSS hex value.
 */
public final class ColorToken extends ThemeToken<ColorToken.Config> {

	/**
	 * Configuration of a {@link ColorToken}.
	 */
	@TagName("color")
	public interface Config extends ThemeToken.Config<ColorToken> {

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/**
		 * The color, given as a hex code ({@code #rrggbb}, {@code #rgb}, {@code #rrggbbaa}) or a
		 * named color.
		 */
		@Name(VALUE)
		@Mandatory
		@Format(ColorConfigFormat.class)
		Color getValue();

	}

	/**
	 * Creates a {@link ColorToken} from configuration.
	 */
	@CalledByReflection
	public ColorToken(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String cssValue() {
		Color color = getConfig().getValue();
		int alpha = color.getAlpha();
		if (alpha == 255) {
			return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		}
		return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

}
