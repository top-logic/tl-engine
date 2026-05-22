/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a toolbar with clique-grouped child controls.
 *
 * <p>
 * The React component {@code TLToolbar} receives:
 * </p>
 * <ul>
 * <li>{@code groups} - ordered list of clique groups, each with display mode and child
 * controls</li>
 * </ul>
 */
public class ReactToolbarControl extends ReactControl {

	private static final String REACT_MODULE = "TLToolbar";

	private static final String GROUPS = "groups";

	private final List<ReactControl> _allChildren = new ArrayList<>();

	private final List<Object> _groups = new ArrayList<>();

	/**
	 * Creates a new empty {@link ReactToolbarControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 */
	public ReactToolbarControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		putState(GROUPS, _groups);
	}

	/**
	 * Adds a clique group to the toolbar.
	 *
	 * @param name
	 *        The clique name.
	 * @param display
	 *        Display mode: "inline" or "menu".
	 * @param label
	 *        Menu trigger label (only for "menu" display, may be {@code null}).
	 * @param icon
	 *        Menu trigger icon (only for "menu" display, may be {@code null}).
	 * @param items
	 *        The child controls in this group.
	 */
	public void addGroup(String name, String display, String label, String icon,
			List<ReactControl> items) {
		Map<String, Object> group = new LinkedHashMap<>();
		group.put("name", name);
		group.put("display", display);
		if (label != null) {
			group.put("label", label);
		}
		if (icon != null) {
			group.put("icon", icon);
		}
		group.put("items", new ArrayList<>(items));
		_groups.add(group);
		putState(GROUPS, _groups);
		_allChildren.addAll(items);
	}

	/**
	 * Whether this toolbar has any groups with items.
	 */
	public boolean isEmpty() {
		return _allChildren.isEmpty();
	}

	/**
	 * Replaces all groups with the groups from another toolbar.
	 *
	 * <p>
	 * Used for reactive toolbar rebuilds when the command scope changes (implicit commands
	 * added/removed). Cleans up old children, adopts the new groups, and pushes the updated
	 * state to the client via SSE.
	 * </p>
	 *
	 * @param newToolbar
	 *        The newly built toolbar whose groups should replace the current ones.
	 */
	public void replaceGroups(ReactToolbarControl newToolbar) {
		// Clean up old children.
		for (ReactControl child : _allChildren) {
			child.cleanupTree();
		}
		_allChildren.clear();
		_groups.clear();

		// Adopt new groups from the rebuilt toolbar.
		_allChildren.addAll(newToolbar._allChildren);
		_groups.addAll(newToolbar._groups);

		// Push full groups state to client.
		putState(GROUPS, _groups);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _allChildren) {
			child.cleanupTree();
		}
	}
}
