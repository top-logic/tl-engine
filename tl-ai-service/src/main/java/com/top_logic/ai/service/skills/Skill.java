/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * An instruction fragment that can be injected into an AI agent's context, either automatically
 * via the system prompt or on demand via {@link SkillTool}.
 *
 * <p>
 * Skills are loaded from classpath resource files ({@code .md}) with optional YAML frontmatter
 * for metadata, and managed by {@link SkillRegistry}.
 * </p>
 *
 * @param id
 *        Unique skill identifier derived from the resource path (e.g. {@code "shared/prompting-ModelExplorer"}).
 *        Used by the agent to call {@code loadSkill(id)}.
 * @param description
 *        Human-readable description from YAML frontmatter.
 *        Defaults to the id if not specified.
 * @param mode
 *        Whether this skill is injected automatically into the system prompt ({@link Mode#AUTO})
 *        or loaded on demand via tool call ({@link Mode#ON_DEMAND}).
 * @param content
 *        The actual prompt text (frontmatter stripped).
 */
public record Skill(String id, String description, Mode mode, String content) {

	/**
	 * Controls how a skill's content is made available to the agent.
	 */
	public enum Mode {

		/**
		 * Full content is injected directly into the system prompt. No tool call needed.
		 */
		AUTO,

		/**
		 * Only the id and description appear in the system prompt catalog.
		 * The agent must call {@code loadSkill(id)} to get the full content.
		 */
		ON_DEMAND;

		/**
		 * Parses a mode from its frontmatter string value.
		 *
		 * @param value
		 *        The string value ({@code "auto"} or {@code "ondemand"}). Case-insensitive.
		 * @return The parsed mode, or {@link #ON_DEMAND} if the value is unrecognized.
		 */
		public static Mode parse(String value) {
			if ("auto".equalsIgnoreCase(value)) {
				return AUTO;
			}
			return ON_DEMAND;
		}
	}
}
