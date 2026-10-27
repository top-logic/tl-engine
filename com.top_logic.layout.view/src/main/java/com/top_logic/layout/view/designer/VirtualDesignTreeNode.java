/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.util.Resources;

/**
 * A {@link DesignTreeNode} grouping child nodes under a label without representing a configuration
 * of its own.
 *
 * <p>
 * A group either stands for a property (e.g. "header", "content", "footer") bundling the child
 * elements stored in it, see {@link #VirtualDesignTreeNode(PropertyDescriptor, String)}, or groups
 * nodes under a fixed label, see {@link #VirtualDesignTreeNode(ResKey, String)}.
 * </p>
 */
public class VirtualDesignTreeNode extends DesignTreeNode {

	private final PropertyDescriptor _property;

	private final ResKey _labelKey;

	/**
	 * Creates a {@link VirtualDesignTreeNode} standing for a property.
	 *
	 * @param property
	 *        The property descriptor this group represents.
	 * @param sourceFile
	 *        The .view.xml file this group belongs to.
	 */
	public VirtualDesignTreeNode(PropertyDescriptor property, String sourceFile) {
		super(sourceFile);
		_property = property;
		_labelKey = null;
	}

	/**
	 * Creates a {@link VirtualDesignTreeNode} with a fixed label.
	 *
	 * @param labelKey
	 *        The label of the group. Its {@link ResKey#tooltip() tooltip}, if present, is the
	 *        tooltip of the group.
	 * @param sourceFile
	 *        The .view.xml file this group belongs to, {@code null} for a group belonging to no
	 *        file.
	 */
	public VirtualDesignTreeNode(ResKey labelKey, String sourceFile) {
		super(sourceFile);
		_property = null;
		_labelKey = labelKey;
	}

	/**
	 * The property descriptor this group represents, or {@code null} for a group with a fixed
	 * {@link #getLabelKey() label}.
	 */
	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	/**
	 * The fixed label of this group, or {@code null} for a group standing for a
	 * {@link #getProperty() property}.
	 */
	public ResKey getLabelKey() {
		return _labelKey;
	}

	@Override
	public String getTagName() {
		return getDisplayLabel();
	}

	@Override
	public String getDisplayLabel() {
		if (_property == null) {
			return Resources.getInstance().getString(_labelKey);
		}
		return Labels.propertyLabel(_property, false);
	}

	@Override
	public String getTooltipHtml() {
		ResKey tooltip = _property == null ? _labelKey.tooltip() : _property.labelKey(null).tooltip();
		return Resources.getInstance().getString(tooltip, null);
	}
}
