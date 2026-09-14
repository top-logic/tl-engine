/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.awt.Color;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.format.ColorConfigFormat;

/**
 * Specification of a color, given either as a literal color value or as the name of a design token
 * of the UI theme.
 *
 * <p>
 * A design token adapts to the active theme, a literal color does not. Where both are given, the
 * literal color wins.
 * </p>
 *
 * @implNote {@link ValueColor#of(ColorSpec)} resolves the specification to the {@link ValueColor}
 *           the UI displays.
 */
public interface ColorSpec extends ConfigurationItem {

	/** Configuration name of {@link #getValue()}. */
	String VALUE = "value";

	/** Configuration name of {@link #getToken()}. */
	String TOKEN = "token";

	/**
	 * The literal color to display with, such as <code>#04a38d</code>.
	 */
	@Name(VALUE)
	@Nullable
	@Format(ColorConfigFormat.class)
	@Label("Color")
	Color getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(Color value);

	/**
	 * The name of the design token holding the color to display with, such as
	 * <code>support-success</code>.
	 *
	 * <p>
	 * The token is looked up in the theme the user has active, so the color follows a theme switch.
	 * The tokens are those defined for the UI themes; the name is given without the leading
	 * <code>--</code> of the CSS custom property the token is emitted as.
	 * </p>
	 */
	@Name(TOKEN)
	@Nullable
	@Label("Design token")
	String getToken();

	/**
	 * @see #getToken()
	 */
	void setToken(String value);

}
