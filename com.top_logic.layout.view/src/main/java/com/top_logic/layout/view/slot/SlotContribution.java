/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot;

import java.util.List;

import com.top_logic.layout.react.control.ReactControl;

/**
 * Server-side view of a {@code <slot-content>} contribution that the {@link SlotRegistry} can
 * route to a matching {@link SlotPlaceholder}. Implemented by the React control, queried by the
 * registry.
 */
public interface SlotContribution {

	/**
	 * The target slot name (the value of the {@code to} attribute on
	 * {@code <slot-content to="...">}).
	 */
	String getSlotName();

	/**
	 * Position of this contribution in the rendered view tree.
	 */
	SlotPath getSlotPath();

	/**
	 * The contributed controls, in declaration order.
	 *
	 * <p>
	 * These are the children of {@code <slot-content>}, already instantiated as controls in the
	 * declaring view's {@link com.top_logic.layout.view.ViewContext} (so channel references resolve
	 * locally). The matched placeholder adopts them for rendering.
	 * </p>
	 */
	List<ReactControl> getControls();

	/**
	 * Whether this contribution is meant to be shown only where the enclosing display offers its
	 * slot, so that reaching no slot is no mistake that the {@link SlotRegistry} reports.
	 */
	boolean isOptional();

	/**
	 * Where this contribution is declared, for the report of a contribution that reaches no slot.
	 */
	String getSource();

	/**
	 * Whether this contribution is marked as reaching no slot.
	 *
	 * @see #setUnrouted(boolean)
	 */
	boolean isUnrouted();

	/**
	 * Marks this contribution as reaching no slot, or removes that mark.
	 *
	 * <p>
	 * Called by the {@link SlotRegistry}: with {@code true} when the interaction that displayed the
	 * contribution has completed without routing it to a slot, with {@code false} when it is routed
	 * to one.
	 * </p>
	 */
	void setUnrouted(boolean unrouted);

}
