/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Configuration options for annotating the display of boolean attributes.
 * 
 * @see BooleanDisplay
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum BooleanPresentation implements ExternallyNamed {
	/**
	 * Display as check box.
	 */
	CHECKBOX("checkbox"),

	/**
	 * Display as switch, a toggle sliding between its two states.
	 *
	 * <p>
	 * A value that is either on or off, such as a setting being active: the switch shows which of
	 * the two it is by the position of its knob and takes a click to move it. A value that may also
	 * be unknown keeps the check box, whose third state says so.
	 * </p>
	 */
	SWITCH("switch"),

	/**
	 * Display as yes/no select field.
	 */
	SELECT("select"),

	/**
	 * Display as yes/no radio buttons.
	 */
	RADIO("radio");

	private final String _externalName;

	private BooleanPresentation(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}