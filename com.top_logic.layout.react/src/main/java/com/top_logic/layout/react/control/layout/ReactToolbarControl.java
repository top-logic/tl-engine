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
 * <li>{@link #GROUPS} - ordered list of clique groups, each with display mode and child
 * controls</li>
 * <li>{@link #OVERFLOW} - the {@link ToolbarOverflow} end at which commands that do not fit
 * collapse</li>
 * </ul>
 *
 * <p>
 * A toolbar holds two kinds of groups: the ones {@link #addGroup(String, ToolbarGroupDisplay,
 * String, String, List) added} for the commands it was built from, which
 * {@link #replaceGroups(ReactToolbarControl)} swaps as a whole, and a single
 * {@link #setPinnedGroup(String, ToolbarGroupDisplay, List) pinned group} that a rebuild keeps.
 * The pinned group leads the {@link #GROUPS} the client receives.
 * </p>
 */
public class ReactToolbarControl extends ReactControl {

	private static final String REACT_MODULE = "TLToolbar";

	/** State key for the ordered clique groups, each with its display mode and child controls. */
	public static final String GROUPS = "groups";

	/** @see #getOverflow() */
	public static final String OVERFLOW = "overflow";

	/** Key of a group's clique name within a {@link #GROUPS} entry. */
	public static final String GROUP_NAME = "name";

	/** Key of a group's {@link ToolbarGroupDisplay} within a {@link #GROUPS} entry. */
	public static final String GROUP_DISPLAY = "display";

	/** Key of the menu trigger label within a {@link #GROUPS} entry. */
	public static final String GROUP_LABEL = "label";

	/** Key of the menu trigger icon within a {@link #GROUPS} entry. */
	public static final String GROUP_ICON = "icon";

	/** Key of a group's child controls within a {@link #GROUPS} entry. */
	public static final String GROUP_ITEMS = "items";

	private final List<ReactControl> _allChildren = new ArrayList<>();

	private final List<Object> _groups = new ArrayList<>();

	/**
	 * The group a {@link #replaceGroups(ReactToolbarControl) rebuild} keeps, or {@code null} while
	 * there is none.
	 *
	 * @see #setPinnedGroup(String, ToolbarGroupDisplay, List)
	 */
	private Map<String, Object> _pinnedGroup;

	private final List<ReactControl> _pinnedChildren = new ArrayList<>();

	private ToolbarOverflow _overflow = ToolbarOverflow.NONE;

	/**
	 * Creates a new empty {@link ReactToolbarControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 */
	public ReactToolbarControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		publishGroups();
		putState(OVERFLOW, _overflow.getExternalName());
	}

	/**
	 * The end at which this toolbar collapses when its commands do not fit.
	 */
	public ToolbarOverflow getOverflow() {
		return _overflow;
	}

	/**
	 * Sets the end at which this toolbar collapses when its commands do not fit.
	 *
	 * @param overflow
	 *        The collapsing behavior, published to the client as {@link #OVERFLOW}.
	 */
	public void setOverflow(ToolbarOverflow overflow) {
		_overflow = overflow;
		putState(OVERFLOW, overflow.getExternalName());
	}

	/**
	 * Adds a clique group to the toolbar.
	 *
	 * @param name
	 *        The clique name.
	 * @param display
	 *        Display mode of the group.
	 * @param label
	 *        Menu trigger label (only for {@link ToolbarGroupDisplay#MENU} display, may be
	 *        {@code null}).
	 * @param icon
	 *        Menu trigger icon (only for {@link ToolbarGroupDisplay#MENU} display, may be
	 *        {@code null}).
	 * @param items
	 *        The child controls in this group.
	 */
	public void addGroup(String name, ToolbarGroupDisplay display, String label, String icon,
			List<ReactControl> items) {
		_groups.add(group(name, display, label, icon, items));
		_allChildren.addAll(items);
		publishGroups();
	}

	/**
	 * Sets the group that leads this toolbar and stays through a rebuild.
	 *
	 * <p>
	 * The pinned group belongs to whoever composes the toolbar, not to the commands it was built
	 * from: {@link #replaceGroups(ReactToolbarControl)} swaps the
	 * {@link #addGroup(String, ToolbarGroupDisplay, String, String, List) added groups} and keeps
	 * this one, so a composer can hand its own controls to a toolbar that rebuilds itself
	 * whenever its command scope changes.
	 * </p>
	 *
	 * <p>
	 * A toolbar has at most one pinned group; a further call replaces it. Empty items leave the
	 * toolbar with none.
	 * </p>
	 *
	 * @param name
	 *        The clique name of the group.
	 * @param display
	 *        Display mode of the group.
	 * @param items
	 *        The child controls in this group.
	 */
	public void setPinnedGroup(String name, ToolbarGroupDisplay display, List<? extends ReactControl> items) {
		_pinnedChildren.clear();
		if (items.isEmpty()) {
			_pinnedGroup = null;
		} else {
			_pinnedGroup = group(name, display, null, null, items);
			_pinnedChildren.addAll(items);
		}
		publishGroups();
	}

	/**
	 * Whether this toolbar has any groups with items.
	 */
	public boolean isEmpty() {
		return _allChildren.isEmpty() && _pinnedChildren.isEmpty();
	}

	/**
	 * Replaces the added groups with the groups from another toolbar.
	 *
	 * <p>
	 * Used for reactive toolbar rebuilds when the command scope changes (implicit commands
	 * added/removed). Cleans up the children of the replaced groups, adopts the new groups, and
	 * pushes the updated state to the client via SSE. The
	 * {@link #setPinnedGroup(String, ToolbarGroupDisplay, List) pinned group} is kept and still
	 * leads the groups afterwards.
	 * </p>
	 *
	 * @param newToolbar
	 *        The newly built toolbar whose groups should replace the current ones.
	 */
	public void replaceGroups(ReactToolbarControl newToolbar) {
		// Clean up the children of the groups being replaced.
		for (ReactControl child : _allChildren) {
			child.cleanupTree();
		}
		_allChildren.clear();
		_groups.clear();

		// Adopt new groups from the rebuilt toolbar.
		_allChildren.addAll(newToolbar._allChildren);
		_groups.addAll(newToolbar._groups);

		// Push full groups state to client.
		publishGroups();

		// The rebuilt toolbar was built for the same placement, so it carries the collapsing
		// behavior this one must keep displaying with.
		setOverflow(newToolbar._overflow);
	}

	private static Map<String, Object> group(String name, ToolbarGroupDisplay display, String label, String icon,
			List<? extends ReactControl> items) {
		Map<String, Object> group = new LinkedHashMap<>();
		group.put(GROUP_NAME, name);
		group.put(GROUP_DISPLAY, display.getExternalName());
		if (label != null) {
			group.put(GROUP_LABEL, label);
		}
		if (icon != null) {
			group.put(GROUP_ICON, icon);
		}
		group.put(GROUP_ITEMS, new ArrayList<>(items));
		return group;
	}

	/**
	 * Publishes the pinned group followed by the added ones as the {@link #GROUPS} state.
	 */
	private void publishGroups() {
		List<Object> published = new ArrayList<>(_groups.size() + 1);
		if (_pinnedGroup != null) {
			published.add(_pinnedGroup);
		}
		published.addAll(_groups);
		putState(GROUPS, published);
	}

}
