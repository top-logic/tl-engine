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
 * acting user's context before the run started, so nothing here is evaluated again.
 * </p>
 *
 * <p>
 * {@link #stable()} says where it is rendered, and it is the only question that decides it. A
 * stable fact goes through the {@code {{context}}} placeholder into the environment block of the
 * system prompt, which must be byte-identical on every turn or the provider's prompt cache misses
 * on each one. A fact that changes while the conversation runs - the current selection, an open
 * dialog - goes into the {@link TurnContext per-turn block}, which travels in the turn itself,
 * beside the user's text.
 * </p>
 *
 * @param name
 *        What the fact is called in front of the model, e.g. {@code "Project"}.
 * @param value
 *        The rendered value, e.g. {@code "Airport expansion"}.
 * @param stable
 *        Whether the value holds for the whole conversation: {@code true} renders it into the
 *        environment block, {@code false} into the per-turn block.
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
