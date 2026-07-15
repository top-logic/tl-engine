/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Display mode of a {@link ReactButtonControl}, controlling whether the button shows its icon, its
 * label, or both.
 */
public enum ButtonDisplayMode implements ExternallyNamed {

	/** Only the theme icon is shown; the label serves as tooltip. */
	ICON_ONLY("icon-only"),

	/** Theme icon and label side by side. */
	ICON_LABEL("icon-label"),

	/** Plain text button without icon. */
	LABEL_ONLY("label-only");

	private final String _externalName;

	ButtonDisplayMode(String externalName) {
		_externalName = externalName;
	}

	/**
	 * The identifier transmitted to the {@code TLButton} React component.
	 */
	@Override
	public String getExternalName() {
		return _externalName;
	}
}
