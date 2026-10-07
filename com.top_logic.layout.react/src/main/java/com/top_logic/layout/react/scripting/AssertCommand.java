/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * An assertion step of a recorded script: on replay, the node at the {@link #getAddress() address}
 * is <em>verified</em> against the recorded {@link #getState() expected state} rather than a command
 * being dispatched.
 *
 * <p>
 * The expected state is a subset of the node's {@link ScriptingTreeProjector#nodeState(
 * com.top_logic.layout.react.control.ReactControl) projected state} — inherently untyped JSON data,
 * carried in its canonical JSON form. {@link #mismatchingKeys(Map, Map)} is the subset check a
 * replay performs.
 * </p>
 */
@Label("Check state of '{target}'")
public interface AssertCommand extends ReactCommand {

	/**
	 * Grants the configuration proxy access to this interface's {@code default} methods.
	 */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * The {@link ReactCommand#getName() command name} of an assertion step.
	 */
	String COMMAND_NAME = "assertState";

	/** @see #getState() */
	String STATE = "state";

	/**
	 * Separator between the steps of a {@link #mismatchingKeys(Map, Map) mismatch path}.
	 */
	String PATH_SEPARATOR = ".";

	/**
	 * The expected state entries as canonical JSON (an object mapping state keys to their expected
	 * values).
	 */
	@Name(STATE)
	@Nullable
	String getState();

	/** @see #getState() */
	void setState(String value);

	/**
	 * The {@link #getState() expected state} parsed back to its entry map; empty if none was
	 * recorded.
	 */
	@SuppressWarnings("unchecked")
	default Map<String, Object> stateEntries() {
		String state = getState();
		if (state == null) {
			return Map.of();
		}
		try {
			return (Map<String, Object>) JSON.fromString(state);
		} catch (JSON.ParseException ex) {
			throw new IllegalArgumentException("Invalid expected state of assertion on '" + getAddress() + "'.", ex);
		}
	}

	/**
	 * An assertion step that, on replay, checks the node at {@code address} has at least the given
	 * expected state entries.
	 *
	 * @param address
	 *        The semantic address of the node to verify.
	 * @param expectedState
	 *        The state entries that must match (subset of the node's full state).
	 * @return The assertion step.
	 */
	static AssertCommand create(String address, Map<String, Object> expectedState) {
		AssertCommand result = TypedConfiguration.newConfigItem(AssertCommand.class);
		result.setName(COMMAND_NAME);
		result.setAddress(address);
		result.setState(JSON.toString(expectedState));
		return result;
	}

	/**
	 * The paths of {@code expected} whose value differs from {@code actual}, comparing by canonical
	 * JSON so numeric/representation differences do not cause false mismatches. An empty result means
	 * {@code actual} satisfies every expected entry (a <em>subset</em> match: keys not in
	 * {@code expected} are ignored). This is the assertion check a replay performs.
	 *
	 * <p>
	 * The subset match reaches into nested objects: where both sides hold a {@link Map}, the expected
	 * map is compared entry by entry against the actual one, so an assertion recorded for one entry
	 * of a grouped state value (a control's
	 * {@link com.top_logic.layout.react.control.ReactControl#DIAGNOSTICS diagnostics}, say) still
	 * holds when a sibling entry of that group changes.
	 * </p>
	 *
	 * @param expected
	 *        The recorded expected state entries.
	 * @param actual
	 *        The node's live state at replay.
	 * @return The mismatching paths, in {@code expected}'s iteration order; empty if the assertion
	 *         holds. A path is the state key, with the keys of the nested maps it descends into
	 *         appended separated by {@link #PATH_SEPARATOR} (e.g. {@code diagnostics.hiddenByAccess}),
	 *         as {@link #valueAt(Map, String)} reads it back.
	 */
	static List<String> mismatchingKeys(Map<String, Object> expected, Map<String, Object> actual) {
		List<String> mismatches = new ArrayList<>();
		collectMismatches("", expected, actual, mismatches);
		return mismatches;
	}

	/**
	 * Implementation of {@link #mismatchingKeys(Map, Map)} for the map at the given path prefix.
	 */
	private static void collectMismatches(String prefix, Map<?, ?> expected, Object actual, List<String> mismatches) {
		Map<?, ?> actualMap = actual instanceof Map<?, ?> map ? map : null;
		for (Map.Entry<?, ?> entry : expected.entrySet()) {
			String path = prefix + entry.getKey();
			Object actualValue = actualMap == null ? null : actualMap.get(entry.getKey());
			if (entry.getValue() instanceof Map<?, ?> expectedValue && actualValue instanceof Map<?, ?>) {
				collectMismatches(path + PATH_SEPARATOR, expectedValue, actualValue, mismatches);
			} else if (!JSON.toString(entry.getValue()).equals(JSON.toString(actualValue))) {
				mismatches.add(path);
			}
		}
	}

	/**
	 * The value a {@link #mismatchingKeys(Map, Map) mismatch path} points to, for reporting what the
	 * assertion expected and what it found.
	 *
	 * @param state
	 *        The expected or the actual state entries.
	 * @param path
	 *        A path as {@link #mismatchingKeys(Map, Map)} reports it.
	 * @return The value at the path, {@code null} if the state has none. A state key containing the
	 *         {@link #PATH_SEPARATOR} is not addressable this way.
	 */
	static Object valueAt(Map<String, Object> state, String path) {
		Object result = state;
		for (String step : path.split(Pattern.quote(PATH_SEPARATOR))) {
			if (!(result instanceof Map<?, ?> map)) {
				return null;
			}
			result = map.get(step);
		}
		return result;
	}
}
