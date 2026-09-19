/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.provider.LLMService;

/**
 * Reads the {@code <user-memory>} section of the {@link LLMService} configuration.
 *
 * <p>
 * Every accessor answers without the service, the way {@link EnvironmentSettings} does: a unit
 * test, a migration or a boot step that runs before the service is up gets the shipped default
 * rather than an exception.
 * </p>
 *
 * @see UserMemoryConfig
 */
public class UserMemorySettings {

	/**
	 * The shipped defaults, as an instance of the configuration interface.
	 *
	 * <p>
	 * Built once from the interface's own {@code @...Default} annotations, so the answer without a
	 * running service is exactly the answer with an unconfigured one - there is no second copy of
	 * the defaults in this class to drift away from the first.
	 * </p>
	 */
	private static final UserMemoryConfig DEFAULTS =
		TypedConfiguration.newConfigItem(UserMemoryConfig.class);

	private UserMemorySettings() {
		// Static utility.
	}

	/**
	 * The section a test put in place of the configured one, or {@code null}.
	 *
	 * @see #override(UserMemoryConfig)
	 */
	private static volatile UserMemoryConfig _override;

	/** The configured section, or the shipped defaults when the service is not running. */
	private static UserMemoryConfig config() {
		UserMemoryConfig overridden = _override;
		if (overridden != null) {
			return overridden;
		}
		if (!LLMService.Module.INSTANCE.isActive()) {
			return DEFAULTS;
		}
		UserMemoryConfig configured = LLMService.getInstance().getUserMemoryConfig();
		return configured == null ? DEFAULTS : configured;
	}

	/**
	 * Answers every question below from the given section until the handle is closed.
	 *
	 * <p>
	 * <b>A test seam.</b> Whether an installation keeps memories is a property of the
	 * {@link LLMService} configuration, and a unit test that wanted to exercise the switched-off
	 * half would otherwise have to boot that service. Nothing in production calls this - the
	 * administrator's switch is the {@code <user-memory>} section, and there is no second way to
	 * set it.
	 * </p>
	 *
	 * @param config
	 *        What to answer from; {@code null} restores the configured section.
	 */
	public static Overridden override(UserMemoryConfig config) {
		UserMemoryConfig before = _override;
		_override = config;
		return () -> _override = before;
	}

	/**
	 * Handle returned by {@link UserMemorySettings#override(UserMemoryConfig)}; closing it restores
	 * what was in place before.
	 */
	@FunctionalInterface
	public interface Overridden extends AutoCloseable {

		@java.lang.Override
		void close();
	}

	/**
	 * Whether this installation keeps memories about its users at all.
	 *
	 * @see UserMemoryConfig#getEnabled()
	 */
	public static boolean enabled() {
		return config().getEnabled();
	}

	/**
	 * How many of an account's newest memories reach the environment block.
	 *
	 * @see UserMemoryConfig#getMaxLoaded()
	 */
	public static int maxLoaded() {
		return config().getMaxLoaded();
	}

	/**
	 * How long one memory's text may be, in characters.
	 *
	 * @see UserMemoryConfig#getMaxLength()
	 */
	public static int maxLength() {
		return config().getMaxLength();
	}

	/**
	 * The words that make a sentence a request to remember something.
	 *
	 * @see UserMemoryConfig#getRequestWords()
	 */
	public static List<String> requestWords() {
		return config().getRequestWords();
	}

	/**
	 * The words with which a user agrees to a question the agent asked.
	 *
	 * @see UserMemoryConfig#getAffirmativeWords()
	 */
	public static List<String> affirmativeWords() {
		return config().getAffirmativeWords();
	}

	/**
	 * The words with which a user refuses; they win over the affirmative ones.
	 *
	 * @see UserMemoryConfig#getNegativeWords()
	 */
	public static List<String> negativeWords() {
		return config().getNegativeWords();
	}
}
