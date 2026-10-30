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
 * controls (in registration order, flattened across contributions). Before it is written, the
 * placeholder lets the registry {@link SlotRegistry#settleRouting() settle} the routing, so that it
 * is written with the contributions the display routes to it.
 * </p>
 *
 * <p>
 * Detached, the placeholder no longer receives routing and drops the controls routed to it, which
 * belong to contributions that may be disposed before it is displayed again.
 * </p>
 *
 * <p>
 * While no contribution is routed here, the placeholder displays its fallback controls instead
 * (the {@link com.top_logic.layout.view.slot.SlotElement.Config#getEmpty() empty} content of the
 * {@code <slot>}). Unlike the routed controls, the fallback controls belong to the placeholder: it
 * detaches them when a contribution replaces them and disposes them together with itself.
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

	/**
	 * The controls displayed while no contribution is routed here. Owned by this placeholder.
	 */
	private final List<ReactControl> _emptyControls;

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
	 * @param emptyControls
	 *        The controls to display while no contribution is routed here. The placeholder takes
	 *        ownership of them.
	 */
	public SlotPlaceholderControl(ReactContext context, String slotName, SlotPath slotPath,
			SlotRegistry slotRegistry, List<? extends ReactControl> emptyControls) {
		super(context, null, REACT_MODULE);
		_slotName = slotName;
		_slotPath = slotPath;
		_slotRegistry = slotRegistry;
		_emptyControls = List.copyOf(emptyControls);
		putState(SLOT_NAME, slotName);
		// The routed controls belong to the <slot-content> contributions that created them; the
		// placeholder only borrows them for rendering and must not dispose them. The fallback
		// controls shown in the same property are disposed by cleanupChildren().
		borrowedState(CHILDREN);
		putState(CHILDREN, _emptyControls);
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
		if (flat.equals(_routedControls)) {
			return;
		}
		boolean showedEmpty = _routedControls.isEmpty();
		_routedControls = flat;
		boolean showsEmpty = flat.isEmpty();

		putState(CHILDREN, showsEmpty ? _emptyControls : flat);

		// A replaced fallback is detached like the content a container stops displaying; shown
		// again, it is attached when it is serialized with the new state.
		if (showedEmpty && !showsEmpty) {
			for (ReactControl control : _emptyControls) {
				control.detach();
			}
		}
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
		// The routed controls are already detached together with this placeholder, as is the
		// fallback; shown again, the fallback is attached with the placeholder until the routing
		// replaces it.
		_routedControls = List.of();
		putState(CHILDREN, _emptyControls);
		super.onDetach();
	}

	/**
	 * Settles the routing of the {@link SlotRegistry}, so that the state written shows the
	 * contributions the display routes to this placeholder.
	 */
	@Override
	protected void onBeforeWrite() {
		super.onBeforeWrite();
		_slotRegistry.settleRouting();
	}

	// Note: attach and detach do reach the routed controls and the displayed fallback controls through
	// the default propagation - they are part of the state this placeholder renders, and a borrowed
	// control is displayed exactly while a placeholder shows it. Only disposal stops here, see the
	// borrowedState() declaration above.

	/**
	 * Disposes the fallback controls in addition to the owned state, since the state property they
	 * are displayed in is borrowed and they are not displayed at all while a contribution is routed
	 * here.
	 */
	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		for (ReactControl control : _emptyControls) {
			control.cleanupTree();
		}
	}

	/**
	 * Structural: this control is a slot placeholder and is elided from the headless projection.
	 */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}
}
