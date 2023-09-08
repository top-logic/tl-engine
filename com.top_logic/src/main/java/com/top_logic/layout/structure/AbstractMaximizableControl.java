/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;

/**
 * Base class for controls that can be maximized.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMaximizableControl<I extends AbstractMaximizableControl<?>> extends WrappingControl<I>
		implements Expandable, ExpandableListener, MaximizedControlListener {

	private final Expandable _model;

	private boolean _cockpitInitialized;

	private CockpitControl _cockpit;

	/**
	 * Creates a {@link MaximizableControl}.
	 * 
	 * @param model
	 *        The model storing the expansion state.
	 * @param commandsByName
	 *        See {@link AbstractControlBase#AbstractControlBase(Map)}.
	 */
	protected AbstractMaximizableControl(Expandable model, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);

		_model = model;
	}

	/**
	 * The model containing the expansion state.
	 */
	@Override
	public Expandable getModel() {
		return _model;
	}

	@Override
	public ExpansionState getExpansionState() {
		return _model.getExpansionState();
	}

	@Override
	public void setExpansionState(ExpansionState newState) {
		_model.setExpansionState(newState);
	}

	/**
	 * Whether this control is currently maximized.
	 */
	public final boolean isMaximized() {
		return getExpansionState() == Expandable.ExpansionState.MAXIMIZED;
	}

	private CockpitControl getCockpit() {
		if (!_cockpitInitialized) {
			_cockpitInitialized = true;
			_cockpit = CockpitControl.getCockpitFor(this);
		}
		return _cockpit;
	}

	/**
	 * Whether this control can be maximized.
	 */
	public boolean hasCockpit() {
		return getCockpit() != null;
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();

		_model.addListener(Expandable.STATE, this);
	}

	@Override
	protected void detachInvalidated() {
		_model.removeListener(Expandable.STATE, this);

		super.detachInvalidated();
	}

	@Override
	public void notifyMaximizedControlChanged(CockpitControl sender, LayoutControl oldValue, LayoutControl newValue) {
		if (isMaximized()) {
			if (newValue != this) {
				// Got externally normalized.
				_model.setExpansionState(Expandable.ExpansionState.NORMALIZED);
			}
		} else {
			if (newValue == this) {
				_model.setExpansionState(Expandable.ExpansionState.MAXIMIZED);
			}
		}
	}

	@Override
	public Bubble notifyExpansionStateChanged(Expandable sender, ExpansionState oldValue, ExpansionState newValue) {
		if (sender != _model) {
			return Bubble.BUBBLE;
		}

		internalNotifyExpansionStateChanged(sender, oldValue, newValue);

		if (changes(oldValue, newValue)) {
			requestRepaint();

			// Forward to control listeners, e.g. flexible flow layout.
			notifyListeners(Expandable.STATE, this, oldValue, newValue);
		}

		return Bubble.BUBBLE;
	}

	/**
	 * Hook that is called, whenever the {@link #getModel()} sends an event about an expansion state
	 * change.
	 * 
	 * @param sender
	 *        See {@link #notifyExpansionStateChanged(Expandable, ExpansionState, ExpansionState)}.
	 * @param oldValue
	 *        See {@link #notifyExpansionStateChanged(Expandable, ExpansionState, ExpansionState)}.
	 * @param newValue
	 *        See {@link #notifyExpansionStateChanged(Expandable, ExpansionState, ExpansionState)}.
	 */
	protected void internalNotifyExpansionStateChanged(Expandable sender, ExpansionState oldValue,
			ExpansionState newValue) {
		if (hasCockpit()) {
			if (newValue == Expandable.ExpansionState.MAXIMIZED) {
				if (canMaximize() && _cockpit.maximize(this)) {
					_cockpit.addListener(CockpitControl.MAXIMIZED_CONTENT, this);
				}
			} else {
				if (_cockpit.normalize(this)) {
					_cockpit.removeListener(CockpitControl.MAXIMIZED_CONTENT, this);
				}
			}
		}
	}

	private boolean changes(ExpansionState oldState, ExpansionState newState) {
		// Note: Whether the display changes must include state taken from the model, because the
		// models state already has changed.
		switch (newState) {
			case MAXIMIZED:
				return canMaximize();
			case MINIMIZED:
				return canCollapse();
			case NORMALIZED:
				switch (oldState) {
					case MAXIMIZED:
						return canMaximize();
					case MINIMIZED:
						return canCollapse();
					case NORMALIZED:
						return false;
				}
				throw new UnreachableAssertion("No such state: " + oldState);
		}
		throw new UnreachableAssertion("No such state: " + newState);
	}

	/**
	 * Whether this control listens to the maximize event of the {@link #getModel()}.
	 * 
	 * <p>
	 * This setting does not influence, whether a maximize button is shown. A
	 * {@link MaximizableControl} has no visual representation but listens to the maximize event of
	 * its {@link #getModel()} that is displayed elsewhere.
	 * </p>
	 */
	protected boolean canMaximize() {
		return true;
	}

	/**
	 * Whether this control can be minimized.
	 */
	protected boolean canCollapse() {
		return false;
	}
}
