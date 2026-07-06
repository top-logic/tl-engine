/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Set;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for tracking loaded skill IDs.
 *
 * <p>
 * Skill IDs are added by {@link SkillTool#loadSkill} during agent execution and read by the chat
 * layer after the run to persist them in session metadata.
 * </p>
 */
public class LoadedSkillsBinding {

	private static final ThreadLocalBinding<Set<String>> SKILLS = new ThreadLocalBinding<>();

	/**
	 * Binds the given set on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(Set<String> skills) {
		return new Binding(SKILLS.bind(skills));
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
		private final ThreadLocalBinding<Set<String>>.Handle _handle;

		Binding(ThreadLocalBinding<Set<String>>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
