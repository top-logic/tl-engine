/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * A reusable behavioral instruction fragment that can be composed into an agent's system prompt.
 *
 * <p>
 * Skills are loaded from classpath resource files ({@code .md}) and assembled by
 * {@link SkillRegistry} into complete system prompts for each agent.
 * </p>
 *
 * @param name
 *        Unique skill identifier (e.g. {@code "core/zero-knowledge"}).
 * @param description
 *        Human-readable description of what this skill enforces.
 * @param content
 *        The actual prompt text loaded from the resource file.
 */
public record Skill(String name, String description, String content) {
}
