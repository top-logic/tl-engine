/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.form.FormMember;

/**
 * Specification of the visibility of an attribute in a dynamic form.
 * 
 * {@link VisibilityConfig#getVisibility()}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("visibility")
public enum FormVisibility implements ExternallyNamed {

	/**
	 * The model element adopts the default settings of its {@link FormMember}.
	 */
	DEFAULT("default"),

	/**
	 * The model element is visible and can be directly edited by the user.
	 */
	EDITABLE("editable"),

	/**
	 * The model element is visible, but cannot directly be edited by the user.
	 */
	READ_ONLY("read-only"),

	/**
	 * The model element is visible, can be directly edited by the user and must not be empty.
	 */
	MANDATORY("mandatory");

	private final String _externalName;

	private FormVisibility(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Sets this {@link FormVisibility} to the given {@link FormMember}.
	 * 
	 * @param member
	 *        The {@link FormMember} to adjust according to this {@link FormVisibility}.
	 */
	public void applyTo(FormMember member) {
		member.setVisible(true);
		switch (this) {
			case DEFAULT:
				// keep settings of the member
				break;
			case EDITABLE:
				member.setImmutable(false);
				member.setMandatory(false);
				break;
			case MANDATORY:
				member.setImmutable(false);
				member.setMandatory(true);
				break;
			case READ_ONLY:
				member.setImmutable(true);
				member.setMandatory(false);
				break;
			default:
				break;
		}
	}
}