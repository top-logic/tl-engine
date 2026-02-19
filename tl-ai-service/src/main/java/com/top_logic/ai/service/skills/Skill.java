/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * An on-demand instruction fragment that can be loaded by an AI agent at runtime
 * via {@link SkillTool}.
 *
 * <p>
 * Skills are loaded from classpath resource files ({@code .md}) with optional YAML frontmatter
 * for the description, and managed by {@link SkillRegistry}.
 * </p>
 *
 * @param name
 *        Unique skill identifier (e.g. {@code "tlscript/enum-comparison"}).
 * @param description
 *        Human-readable description from YAML frontmatter.
 * @param content
 *        The actual prompt text (frontmatter stripped).
 */
public record Skill(String name, String description, String content) {
}
