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
 * Skills are loaded from classpath resource files ({@code .md}) with optional YAML frontmatter for
 * metadata, and managed by {@link SkillRegistry}. Agents discover available skills via a catalog in
 * their system prompt and load them at runtime by calling {@code loadSkill(id)}.
 * </p>
 *
 * @param id
 *        Unique skill identifier derived from the resource path (e.g.
 *        {@code "shared/prompting-ModelExplorer"}). Used by the agent to call
 *        {@code loadSkill(id)}.
 * @param description
 *        Human-readable description from YAML frontmatter. Defaults to the id if not specified.
 * @param content
 *        The actual prompt text (frontmatter stripped).
 */
public record Skill(String id, String description, String content) {
	// No mode distinction — all skills are loaded on demand via loadSkill().
}
