/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot.control;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.slot.SlotContribution;
import com.top_logic.layout.view.slot.SlotHandle;
import com.top_logic.layout.view.slot.SlotPath;
import com.top_logic.layout.view.slot.SlotPlaceholder;
import com.top_logic.layout.view.slot.SlotRegistry;

/**
 * Renders {@code <slot-content>} contributions routed to a {@code <slot>} placeholder by the
 * shared {@link SlotRegistry}.
 *
 * <p>
 * On {@link #onAttach()} the placeholder registers with the registry; on {@link #onDetach()} it
 * unregisters. While registered, the registry calls
 * {@link #setRoutedContributions(java.util.List)} whenever the set of contributions routed to
 * this placeholder changes; the placeholder updates its React state with the current contributed
 * controls (in registration order, flattened across contributions).
 * </p>
 */
public class SlotPlaceholderControl extends ReactControl implements SlotPlaceholder {

	private static final String REACT_MODULE = "TLSlot";

	private static final String CHILDREN = "children";

	private static final String SLOT_NAME = "slotName";

	private final String _slotName;

	private final SlotPath _slotPath;

	private final SlotRegistry _slotRegistry;

	private SlotHandle _handle;

	private List<ReactControl> _routedControls = List.of();

	/**
	 * Creates a slot placeholder.
	 *
	 * @param context
	 *        The React context.
	 * @param slotName
	 *        The slot name (matches the {@code name} attribute on {@code <slot>}).
	 * @param slotPath
	 *        Position of this placeholder in the rendered view tree (captured at creation time).
	 * @param slotRegistry
	 *        The registry to register with on attach.
	 */
	public SlotPlaceholderControl(ReactContext context, String slotName, SlotPath slotPath,
			SlotRegistry slotRegistry) {
		super(context, null, REACT_MODULE);
		_slotName = slotName;
		_slotPath = slotPath;
		_slotRegistry = slotRegistry;
		putState(SLOT_NAME, slotName);
		putState(CHILDREN, _routedControls);
	}

	@Override
	public String getSlotName() {
		return _slotName;
	}

	@Override
	public SlotPath getSlotPath() {
		return _slotPath;
	}

	@Override
	public void setRoutedContributions(List<SlotContribution> contributions) {
		List<ReactControl> flat = new ArrayList<>();
		for (SlotContribution c : contributions) {
			flat.addAll(c.getControls());
		}
		_routedControls = flat;
		putState(CHILDREN, _routedControls);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		_handle = _slotRegistry.registerPlaceholder(this);
	}

	@Override
	protected void onDetach() {
		if (_handle != null) {
			_handle.close();
			_handle = null;
		}
		super.onDetach();
	}

	// Note: propagateAttach/propagateDetach/cleanupChildren are deliberately not overridden.
	// The routed controls are owned by their <slot-content> contributions, which manage their
	// attach/detach/cleanup lifecycle. The placeholder only borrows them for rendering.
}
