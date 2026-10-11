/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.state.TabBarState;
import com.top_logic.layout.view.ViewContext;

/**
 * The page a browser window shows, read and rebuilt on the server.
 *
 * <p>
 * Built on the engine's headless projection ({@link ScriptingSession}): the addresses are the
 * engine's. What this adds is a projection compact enough to read, the view file each part of the
 * page comes from, and a rebuild after a view file changed that keeps the user on the navigation
 * item and tab they were on.
 * </p>
 *
 * <p>
 * Call everything here inside the window's interaction.
 * </p>
 */
public final class WindowPage {

	/**
	 * State keys that name which child a container shows, with the command that switches it.
	 *
	 * <p>
	 * The engine's reload boundary rebuilds a view from scratch, so the place the user was on has to
	 * be put back from outside. The sidebar's state key is private in the engine.
	 * </p>
	 */
	private static final List<Place> PLACES = List.of(
		new Place("activeItemId", ReactSidebarControl.SELECT_ITEM_COMMAND, "itemId"),
		new Place(TabBarState.ACTIVE_TAB_ID__PROP, ReactTabBarControl.SELECT_TAB_COMMAND, "tabId"));

	private static final int MAX_CHILDREN = 25;

	private static final Field VIEW_SOURCE = viewSourceField();

	private WindowPage() {
		// Static utility.
	}

	/**
	 * What the window shows: one line per part of the page, indented by nesting, with its address,
	 * role, name, the view file it comes from and its state.
	 */
	public static String describe(ReactControl root) {
		ScriptingSession session = ScriptingSession.forRoot(root);
		StringBuilder out = new StringBuilder();
		describe(session, session.observe(), 0, out);
		return out.toString();
	}

	private static void describe(ScriptingSession session, ScriptingNodeView node, int depth, StringBuilder out) {
		if (!ScriptingSession.ROOT.equals(node.address())) {
			out.append("  ".repeat(depth)).append('@').append(node.address()).append("  ").append(node.role());
			if (node.name() != null) {
				out.append(" \"").append(clip(node.name())).append('"');
			}
			String view = viewSource(session.resolve(node.address()));
			if (view != null) {
				out.append("  [view ").append(ViewFiles.relative(view)).append(']');
			}
			for (Map.Entry<String, Object> entry : node.state().entrySet()) {
				Object value = entry.getValue();
				if (value != null && !(value instanceof Map)
					&& !(value instanceof Collection<?> list && list.isEmpty()) && !"".equals(value)) {
					out.append(' ').append(entry.getKey()).append("=\"").append(clip(String.valueOf(value))).append('"');
				}
			}
			out.append('\n');
			depth++;
		}
		List<ScriptingNodeView> children = node.children();
		for (int n = 0; n < children.size(); n++) {
			if (n == MAX_CHILDREN) {
				out.append("  ".repeat(depth)).append("... ").append(children.size() - n).append(" more\n");
				break;
			}
			describe(session, children.get(n), depth, out);
		}
	}

	/** The view the window was opened with, its start page. */
	public static String startView(ReactControl root) {
		return first(root, WindowPage::viewSource);
	}

	/**
	 * Rebuilds the parts of the page that show a changed view, through the engine's reload
	 * boundaries, and puts the user back on the navigation item and tab they were on.
	 *
	 * @param resources
	 *        The changed views, as resource paths.
	 */
	public static void rebuild(ReactControl root, Set<String> resources) {
		ViewContext context = first(root, c -> c.getReactContext() instanceof ViewContext own ? own : null);
		if (context == null) {
			return;
		}
		ScriptingSession session = ScriptingSession.forRoot(root);
		List<Shown> shown = new ArrayList<>();
		collect(root, session, shown);
		context.fireViewChanged(resources);
		for (Shown entry : shown) {
			try {
				ReactControl control = ScriptingSession.forRoot(root).resolve(entry.address());
				if (control != null && !entry.id().equals(control.scriptingScalarState().get(entry.place().state()))) {
					control.executeCommand(entry.place().command(), Map.of(entry.place().argument(), entry.id()));
				}
			} catch (RuntimeException ex) {
				// The change removed that place; the rebuild's choice stands.
			}
		}
	}

	private static void collect(ReactControl control, ScriptingSession session, List<Shown> shown) {
		Map<String, Object> state = control.scriptingScalarState();
		for (Place place : PLACES) {
			if (state.get(place.state()) instanceof String id) {
				String address = session.addressOf(control);
				if (address != null) {
					shown.add(new Shown(address, place, id));
				}
			}
		}
		for (ReactControl child : control.scriptingChildren()) {
			collect(child, session, shown);
		}
	}

	/** The first non-null answer of {@code property} in the tree, depth first. */
	private static <T> T first(ReactControl control, Function<ReactControl, T> property) {
		T own = property.apply(control);
		if (own != null) {
			return own;
		}
		for (ReactControl child : control.scriptingChildren()) {
			T found = first(child, property);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * The view file a control is the root of. The engine records it on the control
	 * ({@link ReactControl#setViewSource(String)}) for its designer but offers no getter yet.
	 */
	private static String viewSource(ReactControl control) {
		try {
			return control == null || VIEW_SOURCE == null ? null : (String) VIEW_SOURCE.get(control);
		} catch (IllegalAccessException ex) {
			return null;
		}
	}

	private static Field viewSourceField() {
		try {
			Field field = ReactControl.class.getDeclaredField("_viewSource");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException | RuntimeException ex) {
			Logger.warn("The engine no longer records a control's view file; the page names no views.", ex,
				WindowPage.class);
			return null;
		}
	}

	private static String clip(String text) {
		String line = text.replaceAll("\\s+", " ").strip();
		return line.length() > 100 ? line.substring(0, 97) + "..." : line;
	}

	/** A state that names the child a container shows, and the command that switches to another. */
	private record Place(String state, String command, String argument) {
		// A value.
	}

	/** Which child a container showed before a rebuild. */
	private record Shown(String address, Place place, String id) {
		// A value.
	}
}
