/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.trigger;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;

/**
 * Trigger that fires on explicit user or system request.
 *
 * <p>
 * The background agent runs only when explicitly triggered, for example by a user action in the UI
 * or by another agent. Access can be restricted to specific roles via
 * {@link #getAllowedRoles()}.
 * </p>
 */
public interface OnDemandTrigger extends TriggerConfig {

	/** @see #getAllowedRoles() */
	String ALLOWED_ROLES = "allowed-roles";

	/**
	 * The roles that are permitted to trigger this background agent.
	 *
	 * <p>
	 * If empty, any user may trigger the agent. Role names are matched against the TopLogic role
	 * system.
	 * </p>
	 *
	 * @return The list of allowed role names, may be empty.
	 */
	@Name(ALLOWED_ROLES)
	@Format(CommaSeparatedStrings.class)
	List<String> getAllowedRoles();
}
