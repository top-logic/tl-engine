/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Optional;

/**
 * Matcher for a TLModelPart containing relevant informations about the module, type and attribute.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ModelPartMatch {
	private String _moduleName;

	private String _typeName;

	private String _attributeName;

	/**
	 * Creates a matcher with the given module, type and attribute.
	 */
	public ModelPartMatch(String moduleName, String typeName, String attributeName) {
		_moduleName = moduleName;
		_typeName = typeName;
		_attributeName = attributeName;
	}

	/**
	 * Attribute name
	 */
	public String getAttributeName() {
		return _attributeName;
	}

	/**
	 * @see #getAttributeName()
	 */
	public void setAttributeName(String attributeName) {
		_attributeName = attributeName;
	}

	/**
	 * Type name
	 */
	public String getTypeName() {
		return _typeName;
	}

	/**
	 * @see #getTypeName()
	 */
	public void setTypeName(String typeName) {
		_typeName = typeName;
	}

	/**
	 * Module name
	 */
	public String getModuleName() {
		return _moduleName;
	}

	/**
	 * @see #getModuleName()
	 */
	public void setModuleName(String moduleName) {
		_moduleName = moduleName;
	}

	/**
	 * Last not empty matched TL model part.
	 */
	public Optional<String> getLastNotEmptyMatch() {
		if (_attributeName != null) {
			return Optional.of(_attributeName);
		} else if (_typeName != null) {
			return Optional.of(_typeName);
		} else if (_moduleName != null) {
			return Optional.of(_moduleName);
		} else {
			return Optional.empty();
		}
	}
}
