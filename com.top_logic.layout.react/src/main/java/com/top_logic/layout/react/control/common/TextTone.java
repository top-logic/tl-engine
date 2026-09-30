/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The color role a {@link ReactTextControl} is displayed in.
 *
 * <p>
 * A tone names what the color means, not which color it is: the color comes from the design
 * tokens of the active theme, so the same tone stays legible on a light and on a dark theme.
 * </p>
 */
public enum TextTone implements ExternallyNamed {

	/** The color text is read in (the default). */
	PRIMARY("primary"),

	/** Text of lesser weight than what stands beside it. */
	SECONDARY("secondary"),

	/** An explanation of the value or the field it accompanies. */
	HELPER("helper"),

	/** Text the application draws attention to, in the color interactive elements share. */
	ACCENT("accent"),

	/** An outcome that went well. */
	SUCCESS("success"),

	/** A condition the reader should act on before it becomes a failure. */
	WARNING("warning"),

	/** A failure. */
	ERROR("error"),

	/** Text on a filled surface, which brings its own background color. */
	ON_COLOR("on-color");

	private final String _externalName;

	TextTone(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
