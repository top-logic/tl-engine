/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * An instruction fragment that can be loaded into an AI agent's context on demand via
 * {@link SkillTool}.
 *
 * <p>
 * Skills are stored in the knowledge base. Agents discover available skills via a catalog in their
 * system prompt and load them at runtime by calling {@code loadSkill(id)}.
 * </p>
 *
 * @param id
 *        Unique skill identifier. Used by the agent to call {@code loadSkill(id)}.
 * @param description
 *        Human-readable description shown in the system prompt catalog.
 * @param content
 *        The actual prompt text.
 */
public record Skill(String id, String description, String content) {
	// Immutable value object — no additional methods needed.
}
