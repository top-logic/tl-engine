/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.edit.Labels;

/**
 * A {@link DesignTreeNode} representing a virtual property group (e.g. "header", "content",
 * "footer") that bundles multiple child elements belonging to the same container property.
 */
public class VirtualDesignTreeNode extends DesignTreeNode {

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link VirtualDesignTreeNode}.
	 *
	 * @param property
	 *        The property descriptor this group represents.
	 * @param sourceFile
	 *        The .view.xml file this group belongs to.
	 */
	public VirtualDesignTreeNode(PropertyDescriptor property, String sourceFile) {
		super(sourceFile);
		_property = property;
	}

	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	@Override
	public String getTagName() {
		return Labels.propertyLabel(_property, false);
	}
}
