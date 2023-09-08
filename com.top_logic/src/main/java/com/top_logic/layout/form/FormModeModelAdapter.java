/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.WrappedModel;
import com.top_logic.layout.form.model.AbstractModeModel;
import com.top_logic.layout.form.model.ModeModel;
import com.top_logic.layout.form.model.ModeModelListener;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * Adapts the implicit mode of a {@link FormMember} to an observable
 * {@link ModeModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormModeModelAdapter extends AbstractModeModel implements DisabledPropertyListener,
		ImmutablePropertyListener, VisibilityListener, WrappedModel {
	
	private static final int NO_MODE = -1;
	
	private int modeCache = NO_MODE;
	
	/**
	 * The {@link FormMember} this adapter is based on.
	 */
	private final FormMember impl;

	/**
	 * Creates a {@link FormModeModelAdapter}.
	 * 
	 * @param impl
	 *        The {@link FormMember} to take the {@link FormMember#isDisabled()},
	 *        {@link FormMember#isImmutable()}, and
	 *        {@link FormMember#isVisible()} mode from.
	 */
	public FormModeModelAdapter(FormMember impl) {
		this.impl = impl;
	}

	@Override
	public int getMode() {
		if (modeCache == NO_MODE) {
			// Not attached. Cache read-through.
			return lookupMode();
		} else {
			return modeCache;
		}
	}

	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		return handleEvent(sender);
	}

	@Override
	public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		return handleEvent(sender);
	}

	@Override
	public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		return handleEvent(sender);
	}

	private Bubble handleEvent(Object sender) {
		if (sender == impl) {
			update();
		}
		return Bubble.BUBBLE;
	}


	private void update() {
		// Potential change happened.
		int oldMode = this.modeCache;
		int newMode = initMode();
		
		if (newMode == oldMode) {
			return;
		}
		
		fireModeChange(oldMode, newMode);
	}

	@Override
	public void addModeModelListener(ModeModelListener listener) {
		if (! hasModeModelListeners()) {
			// Attach to model.
			initMode();
			impl.addListener(FormMember.DISABLED_PROPERTY, this);
			impl.addListener(FormMember.VISIBLE_PROPERTY, this);
			impl.addListener(FormMember.IMMUTABLE_PROPERTY, this);
		}
		
		super.addModeModelListener(listener);
	}
	
	@Override
	public void removeModeModelListener(ModeModelListener listener) {
		super.removeModeModelListener(listener);
		
		if (! hasModeModelListeners()) {
			// Detach from model, if the last listener goes away.
			impl.removeListener(FormMember.IMMUTABLE_PROPERTY, this);
			impl.removeListener(FormMember.VISIBLE_PROPERTY, this);
			impl.removeListener(FormMember.DISABLED_PROPERTY, this);
			resetMode();
		}
	}

	/**
	 * Initializes {@link #modeCache} with the current mode of the {@link FormMember} {@link #impl}.
	 */
	private int initMode() {
		int currentMode = lookupMode();
		this.modeCache = currentMode;
		return currentMode;
	}
	
	/**
	 * Retrieves the current mode from the {@link FormMember} {@link #impl}.
	 */
	private int lookupMode() {
		if (! impl.isVisible()) {
			return INVISIBLE_MODE;
		} else if (impl.isImmutable()) {
			return IMMUTABLE_MODE;
		} else if (impl.isDisabled()) {
			return DISABLED_MODE;
		} else {
			return EDIT_MODE;
		}
	}

	/**
	 * Resets the mode cache to {@link #NO_MODE}.
	 */
	private void resetMode() {
		this.modeCache = NO_MODE;
	}

	@Override
	public Object getWrappedModel() {
		if (impl instanceof WrappedModel) {
			return ((WrappedModel) impl).getWrappedModel();
		} else {
			return impl;
		}
	}

}
