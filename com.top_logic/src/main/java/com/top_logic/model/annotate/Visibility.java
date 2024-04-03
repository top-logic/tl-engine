/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.model.form.definition.FormVisibility;

/**
 * Specification of the default visibility of a model element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see FormVisibility
 */
public enum Visibility implements ExternallyNamed {

	/**
	 * The model element is visible and can be directly edited by the user.
	 */
	EDITABLE("editable"),

	/**
	 * The model element is visible and must be entered by the user.
	 * 
	 * <p>
	 * In contrast to making the annotated attribute mandatory, this annotation is not enforced by
	 * the database but only by the form where the attribute is edited. This can be used to make an
	 * attribute mandatory only in some situation.
	 * </p>
	 */
	MANDATORY("mandatory"),

	/**
	 * The model element is visible, but cannot directly be edited by the user.
	 * 
	 * <p>
	 * There may be custom application functionality that allows editing.
	 * </p>
	 */
	READ_ONLY("read-only"),

	/**
	 * The model element is hidden from all generically generated views.
	 * 
	 * <p>
	 * There may be custom application functionality that displays the element.
	 * </p>
	 */
	HIDDEN("hidden");

	private final String _externalName;

	private Visibility(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
