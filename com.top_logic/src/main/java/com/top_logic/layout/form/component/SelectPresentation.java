/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.model.annotate.ui.OptionsPresentation;
import com.top_logic.model.annotate.ui.ReferencePresentation;

/**
 * Possible presentations of a selection in a {@link AbstractSelectorComponent selector component}.
 * 
 * @implNote This options are part of {@link ReferencePresentation} in combination with
 *           {@link OptionsPresentation}.
 * 
 * @see AbstractSelectorComponent.UIOptions#getPresentation()
 */
public enum SelectPresentation implements ExternallyNamed {

	/**
	 * Show the possible values as drop-down list box.
	 */
	DROP_DOWN("drop-down"),

	/**
	 * Show the possible values as pop-up dialog.
	 */
	POP_UP_LIST("pop-up-list"),

	/**
	 * Show the possible values as pop-up table.
	 */
	POP_UP_TABLE("pop-up-table"),

	;

	private final String _externalName;

	private SelectPresentation(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
