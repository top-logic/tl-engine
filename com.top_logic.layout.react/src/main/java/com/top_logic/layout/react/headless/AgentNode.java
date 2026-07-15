/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.control.ReactControl;

/**
 * Optional enrichment a {@link ReactControl} may implement to describe itself to the headless agent
 * interface.
 *
 * <p>
 * The headless layer works generically against any {@link ReactControl} (deriving a role from the
 * React module, a name from common label state, and an action space from the
 * {@link com.top_logic.layout.react.control.ReactCommand @ReactCommandHandler} methods). Implementing this
 * interface lets a control override the headless-typed parts of that projection:
 * </p>
 * <ul>
 * <li>a curated {@link #agentState() state} projection hiding presentation noise, and</li>
 * <li>an {@link #agentActions() action} list with argument schemas so an agent need not guess the
 * shape of a command's {@code arguments} map.</li>
 * </ul>
 *
 * <p>
 * Name, role and structural transparency are declared directly on {@link ReactControl}
 * ({@link ReactControl#agentName()}, {@link ReactControl#agentRole()},
 * {@link ReactControl#agentTransparent()}) so controls can provide them without depending on this
 * headless package. All methods here are optional; returning {@code null} falls back to the generic
 * derivation, keeping adoption incremental.
 * </p>
 */
public interface AgentNode {

	/**
	 * A curated view of this node's data state for a headless consumer.
	 *
	 * @return The state map, or {@code null} to fall back to the control's generic
	 *         {@link ReactControl#agentScalarState() scalar state}.
	 */
	default Map<String, Object> agentState() {
		return null;
	}

	/**
	 * The actions this node offers, with their argument schemas.
	 *
	 * @return The action list, or {@code null} to derive bare actions from the control's
	 *         {@link ReactControl#commandNames() command names}.
	 */
	default List<AgentAction> agentActions() {
		return null;
	}
}
