/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.inspector;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Projects controls of the window a UI-inspector side-window inspects: the window that opened it.
 *
 * <p>
 * The inspected control tree is the one of the inspected window, reached through its update queue,
 * never the inspector's own. Every command of the inspector - picking an element, ascending to the
 * enclosing one, projecting the current one anew - starts here.
 * </p>
 */
public final class InspectorAccess {

	private InspectorAccess() {
		// Static utility.
	}

	/**
	 * The root control of the given window's rendered tree, or {@code null} if the window is gone or
	 * has nothing rendered.
	 *
	 * @param context
	 *        The inspector side-window's context, holding the session's window registry.
	 * @param windowName
	 *        The window whose tree is inspected.
	 */
	public static ReactControl rootControl(ReactContext context, String windowName) {
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null || windowName == null) {
			return null;
		}
		SSEUpdateQueue queue = registry.getQueue(windowName);
		return queue == null ? null : queue.getRootControl();
	}

	/**
	 * The projection of the control at the given address in the given window, or {@code null} if
	 * that window has no tree or nothing sits at that address any more.
	 *
	 * <p>
	 * The way a node is taken anew: the address is resolved against the window's tree as it is now,
	 * so the projection shows the state the control has at this moment.
	 * </p>
	 *
	 * @param context
	 *        The inspector side-window's context.
	 * @param windowName
	 *        The window the address belongs to.
	 * @param address
	 *        The address of the control to project.
	 */
	public static InspectedNode inspectAddress(ReactContext context, String windowName, String address) {
		ReactControl root = rootControl(context, windowName);
		if (root == null || address == null) {
			return null;
		}
		ReactControl control;
		try {
			control = ScriptingSession.forRoot(root).resolve(address);
		} catch (IllegalArgumentException ex) {
			// The tree has changed since the address was taken: nothing sits there any more.
			return null;
		}
		if (control == null) {
			return null;
		}
		return new InspectedNode(windowName, address, ScriptingTreeProjector.project(control, address));
	}

	/**
	 * The projection of the given control of the given window, addressed as the headless interface
	 * addresses it.
	 *
	 * <p>
	 * A control the projection does not address on its own - a cell control, a control under an
	 * elided wrapper - is reported through the innermost enclosing control that does have an
	 * address, so a click anywhere in the window yields a node.
	 * </p>
	 *
	 * @param windowName
	 *        The window the control lives in.
	 * @param root
	 *        The root control of that window's tree.
	 * @param control
	 *        The control to project.
	 * @return The projection, or {@code null} if neither the control nor any of its ancestors is
	 *         addressable.
	 */
	public static InspectedNode inspectControl(String windowName, ReactControl root, ReactControl control) {
		if (root == null || control == null) {
			return null;
		}
		ScriptingSession session = ScriptingSession.forRoot(root);
		String address = addressOf(session, root, control);
		if (address == null) {
			return null;
		}
		ScriptingNodeView view = ScriptingTreeProjector.project(session.resolve(address), address);
		return new InspectedNode(windowName, address, view);
	}

	/**
	 * The address of the given control, or of the innermost enclosing control that has one.
	 *
	 * @implNote {@link ScriptingSession#addressOf(ReactControl)} answers {@code null} for a control
	 *           the visible projection does not contain. The ancestor chain is therefore walked from
	 *           the control outwards, over the raw
	 *           {@link ReactControl#scriptingChildren() child relation} the projection is derived
	 *           from, until one of them is addressable.
	 */
	private static String addressOf(ScriptingSession session, ReactControl root, ReactControl control) {
		List<ReactControl> path = new ArrayList<>();
		if (!appendPath(root, control, path)) {
			return null;
		}
		for (int i = path.size() - 1; i >= 0; i--) {
			String address = session.addressOf(path.get(i));
			if (address != null) {
				return address;
			}
		}
		return null;
	}

	/**
	 * Appends the controls leading from {@code current} to {@code target} (both included) to
	 * {@code path}, and whether the target was found below {@code current} at all.
	 */
	private static boolean appendPath(ReactControl current, ReactControl target, List<ReactControl> path) {
		path.add(current);
		if (current == target) {
			return true;
		}
		for (ReactControl child : current.scriptingChildren()) {
			if (appendPath(child, target, path)) {
				return true;
			}
		}
		path.remove(path.size() - 1);
		return false;
	}
}
