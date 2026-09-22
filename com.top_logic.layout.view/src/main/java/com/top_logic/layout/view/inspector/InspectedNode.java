/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.inspector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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
		return stateEntries(view() == null ? Map.of() : view().state());
	}

	/**
	 * The given state flattened to one entry per leaf value.
	 *
	 * @param state
	 *        The state to flatten, may be {@code null}.
	 * @return The leaves of the state, never {@code null}.
	 * @see #stateEntries()
	 */
	public static List<StateEntry> stateEntries(Map<String, Object> state) {
		List<StateEntry> result = new ArrayList<>();
		collectEntries("", state, result);
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
	 * The part of the inspected control's {@link ScriptingNodeView#state() state} that the given
	 * paths address - the expected state of an assertion about exactly those entries.
	 *
	 * @param paths
	 *        The paths of the wanted leaves, as {@link #stateEntries()} reports them.
	 * @return The state reduced to the addressed leaves, see
	 *         {@link #stateSubset(Map, Collection)}.
	 */
	public Map<String, Object> stateSubset(Collection<String> paths) {
		return stateSubset(view() == null ? Map.of() : view().state(), paths);
	}

	/**
	 * The part of a state that the given paths address.
	 *
	 * <p>
	 * The result keeps the shape of the state: a leaf appears under the keys leading to it, so that
	 * it is compared against the live state of a node by the very key it was taken from (the subset
	 * semantics of {@link AssertCommand#mismatchingKeys(Map, Map)}). Paths sharing a prefix
	 * contribute to the same nested object. A path the state does not have is skipped: what is gone
	 * is not asserted about, so a selection taken from an outdated projection still yields an
	 * assertion about the entries that remain.
	 * </p>
	 *
	 * @param state
	 *        The full state to take the leaves from.
	 * @param paths
	 *        The paths of the wanted leaves, as {@link #stateEntries()} reports them.
	 * @return The state reduced to the addressed leaves; empty if none of them exists.
	 */
	public static Map<String, Object> stateSubset(Map<String, Object> state, Collection<String> paths) {
		Map<String, Object> result = new LinkedHashMap<>();
		if (state == null) {
			return result;
		}
		for (String path : paths) {
			if (path != null) {
				copyPath(state, path, result);
			}
		}
		return result;
	}

	/**
	 * Copies the leaf the path addresses in the given state into the same place of the given target.
	 *
	 * <p>
	 * A key holding the {@link AssertCommand#PATH_SEPARATOR} is resolved as well: the path is not
	 * split up front but matched key by key, an exact key first and a key followed by the separator
	 * afterwards, so that an entry keyed with a qualified name (a type name, say) is found at the
	 * path {@link #stateEntries()} reports for it.
	 * </p>
	 *
	 * @return Whether the path addresses a leaf of the state.
	 */
	private static boolean copyPath(Map<String, Object> state, String path, Map<String, Object> target) {
		if (state.containsKey(path)) {
			target.put(path, state.get(path));
			return true;
		}
		for (Map.Entry<String, Object> entry : state.entrySet()) {
			String key = entry.getKey();
			String prefix = key + AssertCommand.PATH_SEPARATOR;
			if (!path.startsWith(prefix) || !(entry.getValue() instanceof Map<?, ?> nested)) {
				continue;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> typed = (Map<String, Object>) nested;
			// A copy of what a path with the same prefix already contributed, so that the entries
			// collected under one key accumulate without the state itself being written to.
			Map<String, Object> nestedTarget = new LinkedHashMap<>();
			if (target.get(key) instanceof Map<?, ?> collected) {
				for (Map.Entry<?, ?> collectedEntry : collected.entrySet()) {
					nestedTarget.put(String.valueOf(collectedEntry.getKey()), collectedEntry.getValue());
				}
			}
			if (copyPath(typed, path.substring(prefix.length()), nestedTarget)) {
				target.put(key, nestedTarget);
				return true;
			}
		}
		return false;
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
