/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Possible positions of a label.
 * 
 * @see LabelPositionAnnotation
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum LabelPosition implements ExternallyNamed {

	/** The label is displayed before the value. */
	DEFAULT("default"),

	/** The label is displayed after the value. */
	AFTER_VALUE("after-value"),

	/** The label is not displayed at all. */
	HIDE_LABEL("hide-label");
	
	private String _externalName;

	/**
	 * Creates a new {@link LabelPosition}.
	 * 
	 */
	private LabelPosition(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Method to break control flow when receiving an unexpected {@link LabelPosition}.
	 */
	public static RuntimeException noSuchPosition(LabelPosition labelPosition) {
		throw new IllegalArgumentException("Unhandled label position: " + labelPosition);
	}
}
