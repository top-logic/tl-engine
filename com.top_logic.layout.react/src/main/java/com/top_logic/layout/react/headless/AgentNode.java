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
 * {@link com.top_logic.layout.react.control.ReactCommand @ReactCommand} methods). Implementing this
 * interface lets a control override those defaults with semantically meaningful values:
 * </p>
 * <ul>
 * <li>a stable {@link #agentName() name} that survives re-renders and makes addresses readable
 * (e.g. the bound attribute name, or a business key),</li>
 * <li>an explicit {@link #agentRole() role} independent of the React module name,</li>
 * <li>a curated {@link #agentState() state} projection hiding presentation noise, and</li>
 * <li>an {@link #agentActions() action} list with argument schemas so an agent need not guess the
 * shape of a command's {@code arguments} map.</li>
 * </ul>
 *
 * <p>
 * All methods are optional; returning {@code null} from any of them falls back to the generic
 * derivation. This keeps adoption incremental — existing controls need no change, and only those
 * worth addressing precisely opt in.
 * </p>
 */
public interface AgentNode {

	/**
	 * A stable, semantically meaningful local name for this node, used as the discriminator in its
	 * address.
	 *
	 * @return The name, or {@code null} to fall back to generic derivation.
	 */
	default String agentName() {
		return null;
	}

	/**
	 * The semantic role of this node (e.g. {@code "button"}, {@code "field"}, {@code "table"}).
	 *
	 * @return The role, or {@code null} to derive one from the React module.
	 */
	default String agentRole() {
		return null;
	}

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
