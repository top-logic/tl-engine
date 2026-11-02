/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot.control;

import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.slot.SlotContribution;
import com.top_logic.layout.view.slot.SlotHandle;
import com.top_logic.layout.view.slot.SlotPath;
import com.top_logic.layout.view.slot.SlotRegistry;

/**
 * Server-side contribution to a {@code <slot>} placeholder. Renders nothing visible on its own;
 * registers its contributed controls with the shared {@link SlotRegistry} on attach so the
 * registry can route them to the nearest matching placeholder.
 *
 * <p>
 * The contributed controls are created by {@code <slot-content>}'s
 * {@code createControl(...)} call in the declaring view's {@link com.top_logic.layout.view.ViewContext},
 * so their channel references resolve in that local scope rather than in the slot host's scope.
 * </p>
 *
 * <p>
 * A contribution the registry marks as {@link #setUnrouted(boolean) reaching no slot} carries the
 * {@link #putDiagnostic(String, Object) diagnostic} {@link #UNROUTED}, which the UI inspector shows.
 * </p>
 */
public class SlotContentControl extends ReactControl implements SlotContribution {

	/**
	 * Name of the {@link #putDiagnostic(String, Object) diagnostic} marking a contribution that the
	 * interaction displaying it has routed to no slot.
	 */
	public static final String UNROUTED = "unrouted";

	private static final String REACT_MODULE = "TLSlotContent";

	private final String _slotName;

	private final SlotPath _slotPath;

	private final SlotRegistry _slotRegistry;

	private final List<ReactControl> _controls;

	private final boolean _optional;

	private final String _source;

	private SlotHandle _handle;

	/**
	 * Creates a slot contribution.
	 *
	 * @param context
	 *        The React context.
	 * @param slotName
	 *        Target slot name (matches the {@code slot} attribute on {@code <slot-content>}).
	 * @param slotPath
	 *        Position of this contribution in the rendered view tree.
	 * @param slotRegistry
	 *        The registry to register with on attach.
	 * @param controls
	 *        The contributed child controls, already created in the declaring view's context.
	 * @param optional
	 *        See {@link #isOptional()}.
	 * @param source
	 *        See {@link #getSource()}.
	 */
	public SlotContentControl(ReactContext context, String slotName, SlotPath slotPath,
			SlotRegistry slotRegistry, List<? extends ReactControl> controls, boolean optional, String source) {
		super(context, null, REACT_MODULE);
		_slotName = slotName;
		_slotPath = slotPath;
		_slotRegistry = slotRegistry;
		_controls = List.copyOf(controls);
		_optional = optional;
		_source = source;
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
	public List<ReactControl> getControls() {
		return _controls;
	}

	@Override
	public boolean isOptional() {
		return _optional;
	}

	@Override
	public String getSource() {
		return _source;
	}

	@Override
	public boolean isUnrouted() {
		return diagnostics().containsKey(UNROUTED);
	}

	@Override
	public void setUnrouted(boolean unrouted) {
		putDiagnostic(UNROUTED, unrouted ? Boolean.TRUE : null);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		_handle = _slotRegistry.registerContribution(this);
	}

	@Override
	protected void onDetach() {
		if (_handle != null) {
			_handle.close();
			_handle = null;
		}
		super.onDetach();
	}

	@Override
	protected void propagateAttach() {
		// The contributed controls are rendered by the matched placeholder, but their attach
		// lifecycle is owned by this contribution. Propagate attach/detach so the controls'
		// own lifecycle hooks fire when the contribution mounts/unmounts.
		for (ReactControl child : _controls) {
			child.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		for (ReactControl child : _controls) {
			child.detach();
		}
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _controls) {
			child.cleanupTree();
		}
	}
}
