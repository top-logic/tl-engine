/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} arranging a list of children.
 *
 * <p>
 * What a layout container contributes is the placement of its children: the flow of a
 * {@link ReactStackControl stack}, the columns of a {@link ReactGridControl grid}. The children
 * themselves come from outside and are exchanged while the container is displayed, whenever the
 * model the display follows changes, so the list of children and the wrapper each child is placed
 * in are common to every layout.
 * </p>
 */
public abstract class ReactLayoutControl extends ReactControl {

	/** @see #setChildren(List) */
	protected static final String CHILDREN = "children";

	/** @see #setItemClass(String) */
	protected static final String ITEM_CLASS = "itemClass";

	private final List<ReactControl> _children;

	/**
	 * Creates a {@link ReactLayoutControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param reactModule
	 *        The React module identifier rendering the layout on the client.
	 * @param children
	 *        The child controls to arrange.
	 */
	protected ReactLayoutControl(ReactContext context, String reactModule, List<? extends ReactControl> children) {
		super(context, null, reactModule);
		_children = new ArrayList<>(children);
		putState(CHILDREN, _children);
	}

	/**
	 * Replaces the displayed children.
	 *
	 * <p>
	 * A dropped child is not cleaned up automatically, since callers may re-add it later (e.g. an
	 * unchanged item in a refreshed list). Callers that remove a child for good must call
	 * {@link #cleanupTree()} on it themselves.
	 * </p>
	 *
	 * @param children
	 *        The new child controls, replacing the current ones.
	 */
	public void setChildren(List<? extends ReactControl> children) {
		_children.clear();
		_children.addAll(children);
		putState(CHILDREN, new ArrayList<>(_children));
	}

	/**
	 * Wraps each child in an element of the given CSS class, carrying the 0-based position of the
	 * child as the CSS custom property {@code --tl-item-index}.
	 *
	 * <p>
	 * A stylesheet composes a per-item value from that position, a staggered entrance animation
	 * being the case it is meant for. Without a class, the children are placed in the container
	 * directly.
	 * </p>
	 *
	 * @param itemClass
	 *        The CSS class of the wrapper around each child, or {@code null} for no wrapper.
	 */
	public void setItemClass(String itemClass) {
		putState(ITEM_CLASS, itemClass);
	}

	/**
	 * Registers a child control that was created after this container, so that it receives state
	 * updates and dispatches its commands.
	 *
	 * <p>
	 * The children of a layout container are built outside of it - {@link #setChildren(List)} is how
	 * a display following a model exchanges them - so announcing a newly built child is part of the
	 * same contract.
	 * </p>
	 */
	@Override
	public void registerChildControl(ReactControl child) {
		super.registerChildControl(child);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), ITEM_CLASS);
	}

}
