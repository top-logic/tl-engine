/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.model.TLReference;

/**
 * The display of options of a {@link TLReference} in a select dialog in case the reference is
 * displayed as {@link ReferencePresentation#POP_UP pop-up select}.
 * 
 * @see OptionsDisplay
 * @see ReferencePresentation#POP_UP
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum OptionsPresentation implements ExternallyNamed {

	/** Display the options as plain list or tree, depending on the options. */
	PLAIN("plain"),

	/** Display the options as table. */
	TABLE("table")
	;

	private final String _externalName;

	private OptionsPresentation(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Create an {@link IllegalArgumentException} that complains about an unknown/unsupported
	 * {@link OptionsPresentation}.
	 */
	public static IllegalArgumentException unknownPresentation(OptionsPresentation presentation) {
		StringBuilder error = new StringBuilder();
		error.append("Unknown ");
		error.append(OptionsPresentation.class.getName());
		error.append(": ");
		error.append(presentation.getExternalName());
		throw new IllegalArgumentException(error.toString());
	}
}

