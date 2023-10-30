/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.config.annotation.MainProperties;

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
	 * Edit the reference by choosing values in a pop-up dialog.
	 * 
	 * <p>
	 * When this presentation is used the annotation {@link OptionsPresentation} defines how the
	 * options are displayed in the dialog.
	 * </p>
	 * 
	 * @see OptionsPresentation
	 */
	POP_UP("pop-up"),

	/**
	 * Edit the reference by choosing values in a drop-down field.
	 */
	DROP_DOWN("drop-down"),

	/**
	 * Edit the reference by showing all possible values in a vertical list of radio buttons.
	 */
	RADIO("radio"),

	/**
	 * Edit the reference by showing all possible values in a horizontal list of radio buttons.
	 */
	RADIO_INLINE("radio-inline"),

	/**
	 * Display the reference as table with values as rows.
	 * 
	 * <p>
	 * When editing the reference, the rows can be removed or new rows can be chosen in a pop-up
	 * dialog that displays possible values.
	 * </p>
	 * 
	 * <p>
	 * The columns displayed in the table are either specified by the {@link MainProperties}
	 * annotation, or are the default columns of the reference type as globally configured in the
	 * application configuration.
	 * </p>
	 * 
	 * <p>
	 * The presentation of options in the chooser dialog can be customized using the
	 * {@link OptionsPresentation} annotation.
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

