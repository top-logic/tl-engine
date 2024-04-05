/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.basic.ControlCommand;

/**
 * The class {@link ContainerControl} is a {@link LayoutControl} which consists of a {@link List} of
 * {@link LayoutControl}s of arbitrary length.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ContainerControl<I extends ContainerControl<?>> extends AbstractLayoutControl<I> {

	/** the list of children of this {@link ContainerControl} */
	private final List<LayoutControl> _children = new ArrayList<>();

	private final List<? extends LayoutControl> _unmodifiableChildren = Collections.unmodifiableList(_children);

	/**
	 * @see AbstractLayoutControl#AbstractLayoutControl(Map)
	 */
	protected ContainerControl(Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
	}

	/**
	 * This method returns the list of children of this {@link ContainerControl}.
	 * 
	 * @return a list of {@link LayoutControl}s. never <code>null</code>.
	 */
	@Override
	public final List<? extends LayoutControl> getChildren() {
		return this._unmodifiableChildren;
	}

	/**
	 * This method sets the list of children. Also it removes this as parent for each
	 * {@link LayoutControl} in the old children list.
	 * 
	 * @param children
	 *            a list of non-null {@link LayoutControl}s. must not be <code>null</code>.
	 */
	public void setChildren(List<? extends LayoutControl> children) {
		if (children == null) {
			throw new IllegalArgumentException("'children' must not be 'null'.");
		}
		dropChildren();

		for (int n = 0, cnt = children.size(); n < cnt; n++) {
			addChild(children.get(n));
		}
	}

	/**
	 * Removes all children of this {@link ContainerControl}.
	 */
	private final void dropChildren() {
		if (_children.isEmpty()) {
			return;
		}
		for (int index = 0, size = _children.size(); index < size; index++) {
			LayoutControl child = _children.get(index);
			childRemoved(child);
			child.detach();
			((AbstractLayoutControl<?>) child).resetParent();
		}
		_children.clear();
		requestRepaint();
	}

	/**
	 * This method adds a {@link LayoutControl} to the the end of this {@link ContainerControl}.
	 * 
	 * @see #addChild(int, LayoutControl)
	 */
	public void addChild(LayoutControl aLayout) {
		addChild(_children.size(), aLayout);
	}

	/**
	 * This method adds a {@link LayoutControl} to the list of this {@link ContainerControl} at the
	 * given position.
	 * 
	 * @param index
	 *        Index in {@link #getChildren()} to add child.
	 * @param child
	 *        The child to add.
	 */
	public void addChild(int index, LayoutControl child) {
		_children.add(index, child);
		((AbstractLayoutControl<?>) child).initParent(this);
		childAdded(child);
		requestRepaint();
	}

	/**
	 * Removes the given child from the list of children.
	 *
	 * @param child
	 *        The child to remove.
	 * 
	 * @return Whether the child was removed.
	 */
	public boolean removeChild(LayoutControl child) {
		boolean removed = _children.remove(child);
		if (removed) {
			childRemoved(child);
			child.detach();
			((AbstractLayoutControl<?>) child).resetParent();
			requestRepaint();
		}
		return removed;
	}

	/**
	 * Called whenever a new child control was added.
	 * 
	 * @param newChild
	 *        The added child control.
	 */
	protected void childAdded(LayoutControl newChild) {
		// Hook for subclasses.
	}

	/**
	 * Called whenever a child is removed from this layout.
	 * 
	 * @param oldChild
	 *        The child being removed.
	 */
	protected void childRemoved(LayoutControl oldChild) {
		// Hook for subclasses.
	}

	@Override
	public Scrolling getScrolling() {
		// Null, to apply "overflow:hidden" by default in css-class {@link LAYOUT_CONTROL_CSS_CLASS}
		return null;
	}

}
