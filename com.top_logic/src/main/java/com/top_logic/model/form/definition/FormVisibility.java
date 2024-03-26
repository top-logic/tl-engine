/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.form.FormMember;
import com.top_logic.model.annotate.Visibility;

/**
 * Specification of the visibility of an attribute in a dynamic form.
 * 
 * {@link VisibilityConfig#getVisibility()}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see Visibility
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
	 * The model element is visible, but cannot be edited by the user. However, the value is
	 * presented as input field.
	 */
	DISABLED("disabled"),

	/**
	 * The model element is visible, but cannot be edited by the user. The only the value is
	 * displayed without an input field.
	 */
	READ_ONLY("read-only"),

	/**
	 * The model element is visible, can be directly edited by the user and must not be empty.
	 */
	MANDATORY("mandatory"),

	/**
	 * The model element not visible at all.
	 */
	HIDDEN("hidden"),

	;

	private final String _externalName;

	private FormVisibility(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	private static Map<String, FormVisibility> BY_EXTERNAL_NAME;

	static {
		BY_EXTERNAL_NAME = new HashMap<>();
		for (FormVisibility value : values()) {
			BY_EXTERNAL_NAME.put(value.getExternalName(), value);
		}
	}

	/**
	 * The {@link FormVisibility} for a given external name.
	 * 
	 * @see #getExternalName()
	 */
	public static FormVisibility fromExternalName(String name) {
		FormVisibility result = BY_EXTERNAL_NAME.get(name);
		if (result == null) {
			throw new IllegalArgumentException("No such visibility: " + name);
		}
		return result;
	}

	/**
	 * Sets this {@link FormVisibility} to the given {@link FormMember}.
	 * 
	 * @param member
	 *        The {@link FormMember} to adjust according to this {@link FormVisibility}.
	 */
	public void applyTo(FormMember member) {
		switch (this) {
			case DEFAULT:
				// keep settings of the member
				break;
			case EDITABLE:
				member.setVisible(true);
				member.setDisabled(false);
				member.setImmutable(false);
				member.setMandatory(false);
				break;
			case MANDATORY:
				member.setVisible(true);
				member.setDisabled(false);
				member.setImmutable(false);
				member.setMandatory(true);
				break;
			case DISABLED:
				member.setVisible(true);
				member.setMandatory(false);
				member.setDisabled(true);
				break;
			case READ_ONLY:
				member.setVisible(true);
				member.setImmutable(true);
				member.setMandatory(false);
				break;
			case HIDDEN:
				member.setMandatory(false);
				member.setVisible(false);
				break;
		}
	}
}