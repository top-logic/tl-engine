/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

/**
 * Attribute of an {@link Element}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Attribute extends QName {

	private final String value;
	
	/* package protected */Attribute(String namespace, String localName, String value) {
		super(namespace, localName);
		
		assert value != null : "Attribute nodes must not have null values.";
		this.value = value;
	}

	/**
	 * The value of this {@link Attribute}.
	 */
	public String getValue() {
		return value;
	}
	
}
