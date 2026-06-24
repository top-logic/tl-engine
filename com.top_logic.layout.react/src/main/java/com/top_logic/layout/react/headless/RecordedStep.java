/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.LinkedHashMap;
import java.util.Map;

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
