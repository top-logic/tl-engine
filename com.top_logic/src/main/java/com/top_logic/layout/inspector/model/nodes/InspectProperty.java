/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

/**
 * Abstract decomposition of an object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class InspectProperty {

	/**
	 * The name of the property as displayed in the name column.
	 */
	public abstract String name();

	/**
	 * The static type of the property as displayed in the type column.
	 */
	public abstract Object staticType();

	/**
	 * Whether the property is "static" (not bound to an object).
	 */
	public boolean isStatic() {
		return false;
	}

	/**
	 * Whether the property is considered non-public.
	 */
	public boolean isPrivate() {
		return false;
	}

	/**
	 * The value of the property as displayed in the value column.
	 */
	public abstract Object getValue();

	@Override
	public final String toString() {
		return name();
	}

	/**
	 * Whether the {@link InspectorTreeNode#getBusinessObject()} is the default value of this
	 * property.
	 * 
	 * @param valueNode
	 *        The node that displays the {@link #getValue()}.
	 */
	public boolean isDefaultValue(InspectorTreeNode valueNode) {
		return false;
	}

}
