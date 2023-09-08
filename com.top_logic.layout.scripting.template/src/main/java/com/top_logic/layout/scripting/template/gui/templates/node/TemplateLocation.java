/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates.node;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.scripting.template.gui.templates.TemplateTreeBuilder;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * Common base class for application models of the template tree.
 * 
 * @see TemplateTreeBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TemplateLocation {

	private final String _name;

	private TLTreeNode<?> _node;

	private final String _resourceSuffix;

	/**
	 * Creates a {@link TemplateLocation}.
	 * 
	 * @param resourceSuffix
	 *        See {@link #getResourceSuffix()}.
	 */
	public TemplateLocation(String resourceSuffix) {
		_resourceSuffix = resourceSuffix;
		_name = FileUtilities.getFilenameOfResource(resourceSuffix);
	}

	/**
	 * Links this resource back to the tree node in which it is displayed.
	 */
	public void initLocation(TLTreeNode<?> node) {
		_node = node;
	}

	/**
	 * The path suffix relative to the application root folder for templates.
	 */
	public String getResourceSuffix() {
		return _resourceSuffix;
	}

	/**
	 * The local name of this resource.
	 * 
	 * <p>
	 * The local name identifies a resource within its {@link #getParent()}.
	 * </p>
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Whether this resource can safely be cast to {@link TemplateResource}.
	 */
	public abstract boolean isTemplate();

	/**
	 * The parent resource, or <code>null</code> if there is no such parent resource.
	 */
	public TemplateFolder getParent() {
		if (_node == null) {
			return null;
		}
		TLTreeNode<?> parentNode = _node.getParent();
		if (parentNode == null) {
			return null;
		}
		return (TemplateFolder) parentNode.getBusinessObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_resourceSuffix == null) ? 0 : _resourceSuffix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemplateLocation other = (TemplateLocation) obj;
		if (_resourceSuffix == null) {
			if (other._resourceSuffix != null)
				return false;
		} else if (!_resourceSuffix.equals(other._resourceSuffix))
			return false;
		return true;
	}

}
