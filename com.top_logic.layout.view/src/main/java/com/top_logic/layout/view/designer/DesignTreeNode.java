/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * A node in the design tree.
 *
 * <p>
 * Concrete subclasses are {@link ConfigDesignTreeNode} (a node bound to a {@link ConfigurationItem})
 * and {@link VirtualDesignTreeNode} (a virtual property group node).
 * </p>
 */
public abstract class DesignTreeNode {

	private final String _sourceFile;

	private final List<DesignTreeNode> _children = new ArrayList<>();

	private DesignTreeNode _parent;

	private boolean _dirty;

	/**
	 * Creates a {@link DesignTreeNode}.
	 *
	 * @param sourceFile
	 *        The .view.xml file this node belongs to.
	 */
	protected DesignTreeNode(String sourceFile) {
		_sourceFile = sourceFile;
	}

	/**
	 * The source .view.xml file path.
	 */
	public String getSourceFile() {
		return _sourceFile;
	}

	/**
	 * The mutable child nodes list.
	 */
	public List<DesignTreeNode> getChildren() {
		return _children;
	}

	/**
	 * The parent node, or {@code null} for the root.
	 */
	public DesignTreeNode getParent() {
		return _parent;
	}

	/**
	 * Sets the parent node.
	 *
	 * @see DesignTreeBuilder
	 */
	void setParent(DesignTreeNode parent) {
		_parent = parent;
	}

	/**
	 * Whether this node or any descendant has been modified since the last save.
	 */
	public boolean isDirty() {
		return _dirty;
	}

	/**
	 * Marks this node as dirty and propagates the flag upward within the same source file. The
	 * propagation stops at view-file boundaries (when the parent's source file differs from this
	 * node's source file), so that only the actually modified file is marked for saving.
	 */
	public void markDirty() {
		_dirty = true;
		if (_parent != null && _sourceFile != null && _sourceFile.equals(_parent.getSourceFile())) {
			_parent.markDirty();
		}
	}

	/**
	 * Clears the dirty flag on this node (not recursive).
	 */
	public void clearDirty() {
		_dirty = false;
	}

	/**
	 * Disposes this node and all descendants. Subclasses release resources by overriding
	 * {@link #onCleanup()}.
	 */
	public final void cleanup() {
		for (DesignTreeNode child : _children) {
			child.cleanup();
		}
		onCleanup();
	}

	/**
	 * Hook for subclasses to release resources held by this node (e.g. configuration listeners).
	 * The default implementation does nothing.
	 */
	protected void onCleanup() {
		// Hook for subclasses.
	}

	/**
	 * The tag name for display.
	 */
	public abstract String getTagName();

	/**
	 * An identifying label for display, or {@code null} when not applicable.
	 */
	public String getLabel() {
		return null;
	}

	/**
	 * The {@link PropertyDescriptor} of a virtual group node, or {@code null} for config nodes.
	 */
	public PropertyDescriptor getProperty() {
		return null;
	}

	@Override
	public String toString() {
		String label = getLabel();
		if (label != null) {
			return getTagName() + " \"" + label + "\"";
		}
		return getTagName();
	}
}
