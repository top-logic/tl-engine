/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.basic.util.Utils;
import com.top_logic.layout.WrappedModel;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.AbstractModeModel;
import com.top_logic.layout.form.model.ModeModel;
import com.top_logic.layout.form.model.ModeModelListener;

/**
 * Adapts the mode of an {@link EditComponent} to the {@link ModeModel} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentModeAdapter extends AbstractModeModel implements ChannelListener, WrappedModel {

	private static final int NO_MODE = -1;

	private final EditComponent component;
	
	private int modeCache = NO_MODE;

	public ComponentModeAdapter(EditComponent component) {
		this.component = component;
	}

	@Override
	public int getMode() {
		if (modeCache == NO_MODE) {
			return fetchMode();
		} else {
			return modeCache;
		}
	}

	@Override
	public void addModeModelListener(ModeModelListener listener) {
		if (! hasModeModelListeners()) {
			initMode();
		}
		
		super.addModeModelListener(listener);
	}
	
	@Override
	public void removeModeModelListener(ModeModelListener listener) {
		super.removeModeModelListener(listener);
		
		if (! hasModeModelListeners()) {
			resetMode();
		}
	}

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldComponentMode, Object newComponentMode) {
		int newMode;
		if (Utils.isTrue((Boolean) newComponentMode)) {
			newMode = EDIT_MODE;
		} else {
			newMode = IMMUTABLE_MODE;
		}
		
		int oldMode = modeCache;
		modeCache = newMode;
		
		fireModeChange(oldMode, newMode);
	}

	private void resetMode() {
		this.modeCache = NO_MODE;
	}

	private void initMode() {
		this.modeCache = fetchMode();
	}
	
	private int fetchMode() {
		if (component.isInViewMode()) {
			return IMMUTABLE_MODE;
		}
		return EDIT_MODE;
	}
	
	@Override
	public Object getWrappedModel() {
		if (component instanceof WrappedModel) {
			return ((WrappedModel) component).getWrappedModel();
		} else {
			return component;
		}
	}

	
}
