/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * The expected state is a subset of the node's {@link AgentTreeProjector#nodeState(
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
	 * The keys of {@code expected} whose value differs from {@code actual}, comparing by canonical JSON
	 * so numeric/representation differences do not cause false mismatches. An empty result means
	 * {@code actual} satisfies every expected entry (a <em>subset</em> match: keys not in
	 * {@code expected} are ignored). This is the assertion check a replay performs.
	 *
	 * @param expected
	 *        The recorded expected state entries.
	 * @param actual
	 *        The node's live state at replay.
	 * @return The mismatching keys, in {@code expected}'s iteration order; empty if the assertion holds.
	 */
	static List<String> mismatchingKeys(Map<String, Object> expected, Map<String, Object> actual) {
		List<String> mismatches = new ArrayList<>();
		for (Map.Entry<String, Object> entry : expected.entrySet()) {
			String expectedJson = JSON.toString(entry.getValue());
			String actualJson = JSON.toString(actual == null ? null : actual.get(entry.getKey()));
			if (!expectedJson.equals(actualJson)) {
				mismatches.add(entry.getKey());
			}
		}
		return mismatches;
	}
}
