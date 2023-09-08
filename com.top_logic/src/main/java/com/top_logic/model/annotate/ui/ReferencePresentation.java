/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;

/**
 * Enumeration of the possible kinds to display the options and value of a {@link TLReference}.
 * 
 * @see ReferenceDisplay
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 * 
 * @see ClassificationPresentation
 * @see BooleanPresentation
 */
public enum ReferencePresentation implements ExternallyNamed {

	/**
	 * Show the possible values as pop up dialog.
	 * 
	 * <p>
	 * When this presentation is used an {@link OptionsPresentation} defines how the options are
	 * displayed in the "option choose dialog".
	 * </p>
	 * 
	 * @see OptionsPresentation
	 */
	POP_UP("pop-up"),

	/**
	 * Show the possible values as drop down box.
	 * */
	DROP_DOWN("drop-down"),

	/**
	 * Shows all possible values using a vertical list of radio buttons.
	 */
	RADIO("radio"),

	/**
	 * Shows all possible values using horizontal list of radio buttons.
	 */
	RADIO_INLINE("radio-inline"),

	/**
	 * Show a table of values with an option to choose new ones and remove old ones.
	 * 
	 * <p>
	 * The columns displayed by default are either specified by the MainColumns annotation, or are
	 * the main columns of the reference type as globally configured in the application
	 * configuration.
	 * </p>
	 * 
	 * <p>
	 * When this presentation is used an {@link OptionsPresentation} defines how the options are
	 * displayed in the "option choose dialog".
	 * </p>
	 */
	TABLE("table"),

	;

	private final String _externalName;

	private ReferencePresentation(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Create an {@link IllegalArgumentException} that complains about an unknown/unsupported
	 * {@link ReferencePresentation}.
	 */
	public static IllegalArgumentException unknownPresentation(ReferencePresentation presentation) {
		StringBuilder error = new StringBuilder();
		error.append("Unknown ");
		error.append(ReferencePresentation.class.getName());
		error.append(": ");
		error.append(presentation.getExternalName());
		throw new IllegalArgumentException(error.toString());
	}

}

