/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How the editor shows an object reference inserted by a {@link Mention}.
 *
 * <p>
 * Both presentations store the same object link in the text; they differ in how the reference is
 * edited.
 * </p>
 */
public enum MentionPresentation implements ExternallyNamed {

	/**
	 * A single unit standing for the object, removed as a whole and not edited character by
	 * character.
	 */
	CHIP("chip"),

	/**
	 * An ordinary link whose text is edited like the text around it.
	 */
	LINK("link");

	private final String _externalName;

	private MentionPresentation(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
