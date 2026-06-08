/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Set;

/**
 * Thread-local binding for tracking loaded skill IDs.
 *
 * <p>
 * Skill IDs are added by {@link SkillTool#loadSkill} during agent execution and read by the chat
 * layer after the run to persist them in session metadata.
 * </p>
 */
public class LoadedSkillsBinding {

	private static final ThreadLocal<Set<String>> SKILLS = new ThreadLocal<>();

	/**
	 * Binds the given set on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(Set<String> skills) {
		Set<String> previous = SKILLS.get();
		if (skills == null) {
			SKILLS.remove();
		} else {
			SKILLS.set(skills);
		}
		return new Binding(previous);
	}

	/**
	 * Returns the set bound to the current thread, or {@code null}.
	 */
	public static Set<String> getSkills() {
		return SKILLS.get();
	}

	/**
	 * Adds a skill ID to the bound set. No-op if no set is bound.
	 */
	public static void addSkill(String skillId) {
		Set<String> skills = SKILLS.get();
		if (skills != null) {
			skills.add(skillId);
		}
	}

	/**
	 * Handle returned by {@link LoadedSkillsBinding#bind(Set)}.
	 */
	public static final class Binding implements AutoCloseable {
		private final Set<String> _previous;
		private boolean _closed;

		Binding(Set<String> previous) {
			_previous = previous;
		}

		@Override
		public void close() {
			if (_closed) {
				return;
			}
			_closed = true;
			if (_previous == null) {
				SKILLS.remove();
			} else {
				SKILLS.set(_previous);
			}
		}
	}
}
