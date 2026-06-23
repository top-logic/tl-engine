/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

/**
 * Schema description of a single argument of an {@link AgentAction}.
 *
 * <p>
 * The headless agent interface advertises the parameters an action accepts so that a consumer (the
 * script recorder or an AI agent) can construct a valid argument map without guessing. The
 * {@link #type()} is an informal type hint (e.g. {@code "string"}, {@code "boolean"},
 * {@code "number"}, {@code "objectId"}), not a Java type.
 * </p>
 *
 * @param name
 *        The argument key, as expected in the {@code arguments} map of a command invocation.
 * @param type
 *        Informal type hint for the value.
 * @param required
 *        Whether the argument must be supplied.
 * @param description
 *        Human/agent readable description of the argument's meaning.
 */
public record AgentParam(String name, String type, boolean required, String description) {

	/**
	 * Convenience factory for a required string parameter.
	 *
	 * @param name
	 *        The argument key.
	 * @param description
	 *        Description of the argument.
	 * @return A required {@code "string"} typed parameter.
	 */
	public static AgentParam requiredString(String name, String description) {
		return new AgentParam(name, "string", true, description);
	}
}
