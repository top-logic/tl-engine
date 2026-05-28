/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot;

import java.util.List;

/**
 * Server-side view of a {@code <slot>} placeholder that the {@link SlotRegistry} routes
 * contributions to. Implemented by the React control, queried by the registry.
 */
public interface SlotPlaceholder {

	/**
	 * The slot name (must match a {@code <slot-content to="X">} for routing).
	 */
	String getSlotName();

	/**
	 * Position of this placeholder in the rendered view tree.
	 */
	SlotPath getSlotPath();

	/**
	 * Replaces the currently routed contributions. Called by the registry whenever the routing for
	 * this placeholder changes.
	 *
	 * <p>
	 * The list contains the {@link SlotContribution#getControls() controls} of every contribution
	 * routed to this placeholder, in registration order, flattened across contributions.
	 * </p>
	 */
	void setRoutedContributions(List<SlotContribution> contributions);

}
