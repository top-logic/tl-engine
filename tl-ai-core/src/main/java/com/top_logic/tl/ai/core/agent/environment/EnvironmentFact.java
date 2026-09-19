/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

/**
 * One fact the host application states about the situation the run happens in.
 *
 * <p>
 * A fact is a name and an already rendered value - the expression that computed it ran in the
 * acting user's context before the run started, so nothing here is evaluated again. Facts reach the
 * model through the {@code {{context}}} placeholder of the environment block.
 * </p>
 *
 * <p>
 * {@link #stable()} says whether the value holds for the whole conversation. Only stable facts go
 * into the environment block, because that block must be byte-identical on every turn or the
 * provider's prompt cache misses on each one. A fact that changes while the conversation runs - the
 * current selection, an open dialog - belongs in the per-turn block instead (Gitea #438), which
 * this release does not render yet.
 * </p>
 *
 * @param name
 *        What the fact is called in front of the model, e.g. {@code "Project"}.
 * @param value
 *        The rendered value, e.g. {@code "Airport expansion"}.
 * @param stable
 *        Whether the value holds for the whole conversation.
 */
public record EnvironmentFact(String name, String value, boolean stable) {

	/** */
	public EnvironmentFact {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("A fact must have a name.");
		}
		value = value == null ? "" : value;
	}
}
