/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.inspector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.scripting.AssertCommand;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;

/**
 * What the UI inspector shows: one control of an inspected window, as the headless projection sees
 * it.
 *
 * <p>
 * The value the inspector's {@code node} channel holds. It pairs the projection with the window it
 * was taken in and the address it was taken at, so the inspector can project the same control anew
 * (refresh) or its parent, both of which need the window, not only the address.
 * </p>
 *
 * @param windowName
 *        The window the inspected control lives in.
 * @param address
 *        The semantic address of the inspected control within that window.
 * @param view
 *        The projection of the control taken at that address.
 */
public record InspectedNode(String windowName, String address, ScriptingNodeView view) {

	/**
	 * One leaf of a node's {@link ScriptingNodeView#state() state}, addressed by its dotted path.
	 *
	 * @param path
	 *        The state keys leading to the leaf, joined with {@link AssertCommand#PATH_SEPARATOR}
	 *        (e.g. {@code diagnostics.hiddenByAccess.count}) - the path an assertion addresses the
	 *        value by.
	 * @param value
	 *        The leaf value in its canonical JSON form.
	 */
	public record StateEntry(String path, String value) {
		// Pure value type.
	}

	/**
	 * The {@link ScriptingNodeView#state() state} of the inspected control flattened to one entry
	 * per leaf value.
	 *
	 * <p>
	 * A nested object contributes its leaves under the path of the keys leading to them; a list, a
	 * scalar and an object without entries are leaves themselves. The entries appear in the order
	 * the state map yields them, nested objects in place of the key holding them.
	 * </p>
	 *
	 * @return The leaves of the state, never {@code null}.
	 */
	public List<StateEntry> stateEntries() {
		List<StateEntry> result = new ArrayList<>();
		collectEntries("", view() == null ? Map.of() : view().state(), result);
		return result;
	}

	private static void collectEntries(String prefix, Map<String, Object> state, List<StateEntry> result) {
		if (state == null) {
			return;
		}
		for (Map.Entry<String, Object> entry : state.entrySet()) {
			String path = prefix + entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Map<?, ?> nested && !nested.isEmpty()) {
				@SuppressWarnings("unchecked")
				Map<String, Object> typed = (Map<String, Object>) nested;
				collectEntries(path + AssertCommand.PATH_SEPARATOR, typed, result);
			} else {
				result.add(new StateEntry(path, JSON.toString(value)));
			}
		}
	}

	/**
	 * The address of the control enclosing the inspected one, or {@code null} if it has none.
	 *
	 * @see #parentAddress(String)
	 */
	public String parentAddress() {
		return parentAddress(address());
	}

	/**
	 * The address of the control enclosing the one at the given address: the address without its
	 * last segment.
	 *
	 * @param address
	 *        The address to ascend from, may be {@code null}.
	 * @return The enclosing control's address, or {@code null} if there is no enclosing control -
	 *         the given address names a top-level control, the synthetic
	 *         {@link ScriptingSession#ROOT root} (which is no control), or nothing at all.
	 */
	public static String parentAddress(String address) {
		if (address == null) {
			return null;
		}
		int lastSegment = address.lastIndexOf(ScriptingTreeProjector.SEPARATOR);
		if (lastSegment <= 0) {
			// A top-level address ("/panel"), the root itself ("/"), or no address at all: the
			// enclosing node is the synthetic root, which no control corresponds to.
			return null;
		}
		return address.substring(0, lastSegment);
	}
}
