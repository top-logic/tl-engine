/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * A reusable behavioral instruction fragment that can be composed into an agent's system prompt
 * or loaded on demand by the AI via {@link SkillTool}.
 *
 * <p>
 * Skills are loaded from classpath resource files ({@code .md}) with YAML frontmatter
 * and assembled by {@link SkillRegistry}.
 * </p>
 *
 * @param name
 *        Unique skill identifier (e.g. {@code "core/zero-knowledge"}).
 * @param description
 *        Human-readable description from YAML frontmatter.
 * @param tier
 *        Whether this skill is {@link SkillTier#CORE} or {@link SkillTier#ON_DEMAND}.
 * @param content
 *        The actual prompt text (frontmatter stripped).
 */
public record Skill(String name, String description, SkillTier tier, String content) {
}
