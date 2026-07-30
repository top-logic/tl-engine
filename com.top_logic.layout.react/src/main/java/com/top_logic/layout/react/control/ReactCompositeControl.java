/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;

/**
 * A {@link ReactControl} that tracks child controls and cleans them up automatically.
 *
 * <p>
 * This is a generic lifecycle container for composite React controls. It stores a list of children,
 * puts them into the React state as {@code "children"}, and overrides {@link #cleanupChildren()} to
 * call {@link ReactControl#cleanupTree()} on each child.
 * </p>
 *
 * <p>
 * Subclasses add domain-specific state and commands. Non-child cleanup (e.g. model detach) is
 * handled by registering actions via {@link #addCleanupAction(Runnable)}.
 * </p>
 */
public class ReactCompositeControl extends ReactControl {

	private static final String CHILDREN = "children";

	private final List<ReactControl> _children;

	/**
	 * Creates a {@link ReactCompositeControl} with initial children.
	 *
	 * @param context
	 *        The React context.
	 * @param model
	 *        The server-side model object.
	 * @param reactModule
	 *        The React module identifier.
	 * @param children
	 *        The initial child controls.
	 */
	public ReactCompositeControl(ReactContext context, Object model, String reactModule,
			List<? extends ReactControl> children) {
		super(context, model, reactModule);
		_children = new ArrayList<>(children);
		putState(CHILDREN, _children);
	}

	/**
	 * Creates a {@link ReactCompositeControl} with no initial children.
	 *
	 * <p>
	 * Children can be added later via {@link #addChild(ReactControl)}.
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param model
	 *        The server-side model object.
	 * @param reactModule
	 *        The React module identifier.
	 */
	public ReactCompositeControl(ReactContext context, Object model, String reactModule) {
		this(context, model, reactModule, List.of());
	}

	/**
	 * Adds a child control.
	 *
	 * @param child
	 *        The child control to add.
	 */
	public void addChild(ReactControl child) {
		_children.add(child);
		putState(CHILDREN, _children);
	}

	/**
	 * The child controls.
	 */
	protected List<ReactControl> getChildren() {
		return _children;
	}

	/**
	 * Replaces all child controls with a new list.
	 *
	 * <p>
	 * Children that are gone are {@link ReactControl#detach() detached}, the new ones are
	 * {@link ReactControl#attach() attached} if this control is displayed. A child contained in both
	 * lists keeps its display state.
	 * </p>
	 *
	 * <p>
	 * Disposing the replaced children stays the caller's responsibility (see
	 * {@link ReactControl#cleanupTree()}): this method is also reachable from inside an observer
	 * notification, where a synchronous disposal of the old subtree would tear down controls whose
	 * listeners are still pending in the notifying source's listener snapshot.
	 * </p>
	 *
	 * @param newChildren
	 *        The new child controls.
	 */
	protected void replaceChildren(List<? extends ReactControl> newChildren) {
		List<ReactControl> removed = new ArrayList<>(_children);
		removed.removeAll(newChildren);

		_children.clear();
		_children.addAll(newChildren);
		putState(CHILDREN, _children);

		// Hand over the display: what is gone is no longer displayed, what took its place is. Both
		// halves matter for everything keyed on the display state - e.g. a form contributes its
		// commands to the enclosing toolbar only while displayed, so a replaced form would otherwise
		// leave its buttons behind and the replacing one would never show its own.
		for (ReactControl child : removed) {
			child.detach();
		}
		if (isAttached()) {
			for (ReactControl child : _children) {
				child.attach();
			}
		}
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
	}

	@Override
	protected void propagateAttach() {
		for (ReactControl child : _children) {
			child.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		for (ReactControl child : _children) {
			child.detach();
		}
	}

}
