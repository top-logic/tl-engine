/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Tone of a {@link ReactButtonControl}: whether the action it triggers is destructive.
 *
 * <p>The tone recolors the chosen {@link ButtonAppearance}; it is not an appearance of its own.
 * It is not combined with {@link ButtonAppearance#LINK}.</p>
 */
public enum ButtonTone implements ExternallyNamed {
	/** An ordinary action. */
	DEFAULT("default"),
	/** A destructive action: delete, discard, revoke. */
	DANGER("danger");

	private final String _externalName;

	ButtonTone(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
