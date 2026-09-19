/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.List;

import com.top_logic.tl.ai.core.provider.LLMService;

/**
 * Reads the {@code <object-ids>} section of the {@link LLMService} configuration.
 *
 * <p>
 * Every accessor answers without the service, the way
 * {@code com.top_logic.tl.ai.core.agent.environment.UserMemorySettings} does: a unit test, a
 * migration or a boot step that runs before the service is up gets the shipped default rather than
 * an exception. The shipped default is "no hints", which leaves the built-in classification of
 * {@link TidDiagnostics} in sole charge - and it is no configuration item either, so a diagnosis is
 * written where the typed configuration itself is not up yet.
 * </p>
 *
 * @see ObjectIdConfig
 */
public class ObjectIdSettings {

	/**
	 * The section a test put in place of the configured one, or {@code null}.
	 *
	 * @see #override(ObjectIdConfig)
	 */
	private static volatile ObjectIdConfig _override;

	private ObjectIdSettings() {
		// Static utility.
	}

	/**
	 * Answers every question below from the given section until the handle is closed.
	 *
	 * <p>
	 * <b>A test seam.</b> Which codes an installation speaks is a property of the
	 * {@link LLMService} configuration, and a unit test about the hook would otherwise have to boot
	 * that service. Nothing in production calls this - the administrator's switch is the
	 * {@code <object-ids>} section, and there is no second way to set it.
	 * </p>
	 *
	 * @param config
	 *        What to answer from; {@code null} restores the configured section.
	 */
	public static Overridden override(ObjectIdConfig config) {
		ObjectIdConfig before = _override;
		_override = config;
		return () -> _override = before;
	}

	/**
	 * Handle returned by {@link ObjectIdSettings#override(ObjectIdConfig)}; closing it restores what
	 * was in place before.
	 */
	@FunctionalInterface
	public interface Overridden extends AutoCloseable {

		@java.lang.Override
		void close();
	}

	/**
	 * The application's own explanations for identifiers that are not object ids, in declaration
	 * order.
	 *
	 * @see ObjectIdConfig#getHints()
	 */
	public static List<ObjectIdConfig.HintConfig> hints() {
		ObjectIdConfig overridden = _override;
		if (overridden != null) {
			return overridden.getHints();
		}
		if (!LLMService.Module.INSTANCE.isActive()) {
			return List.of();
		}
		ObjectIdConfig configured = LLMService.getInstance().getObjectIdConfig();
		return configured == null ? List.of() : configured.getHints();
	}
}
