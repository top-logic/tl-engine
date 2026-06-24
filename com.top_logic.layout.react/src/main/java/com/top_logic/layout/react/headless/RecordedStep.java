/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.json.JSON;

/**
 * One captured interaction: a {@code command} with its {@code arguments}, invoked on the control at a
 * semantic {@code address}.
 *
 * <p>
 * A step is the unit of a recorded script and is replayed by {@link AgentSession#act(String, String,
 * Map)}. The address is the stable semantic path (not a session-allocated control id), so the step
 * resolves again in a later run.
 * </p>
 *
 * @param address
 *        The semantic address of the target control (as {@link AgentSession#resolve(String)}
 *        accepts), or {@code null} if the target could not be addressed.
 * @param command
 *        The command id that was invoked.
 * @param arguments
 *        The command arguments (never {@code null}; an empty map for argument-less commands).
 */
public record RecordedStep(String address, String command, Map<String, Object> arguments) {

	/**
	 * Reserved pseudo-command for an assertion step: on replay the node's state is <em>verified</em>
	 * against the recorded expectation rather than a command being dispatched. The expected state is
	 * carried under the {@value #ASSERT_STATE_ARG} argument.
	 */
	public static final String ASSERT_COMMAND = "assertState";

	/**
	 * Argument key under which an {@link #ASSERT_COMMAND assertion} step carries its expected state
	 * map.
	 */
	public static final String ASSERT_STATE_ARG = "state";

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
	public static RecordedStep assertion(String address, Map<String, Object> expectedState) {
		return new RecordedStep(address, ASSERT_COMMAND, Map.of(ASSERT_STATE_ARG, expectedState));
	}

	/**
	 * Whether this is an {@link #ASSERT_COMMAND assertion} step (verified on replay) rather than an
	 * action step (dispatched on replay).
	 */
	public boolean isAssertion() {
		return ASSERT_COMMAND.equals(command);
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
	public static List<String> mismatchingKeys(Map<String, Object> expected, Map<String, Object> actual) {
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

	/**
	 * This step as an ordered map for JSON serialization.
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("address", address);
		result.put("command", command);
		result.put("arguments", arguments == null ? Map.of() : arguments);
		return result;
	}
}
