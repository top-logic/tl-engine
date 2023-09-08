/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.buttonbar;

import java.util.List;

import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.model.ModelChangeListener;

/**
 * {@link ButtonBarModel}, based on a {@link ButtonComponent}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ButtonComponentBarModel implements ButtonBarModel {

	private ButtonComponent _buttonComponent;

	/**
	 * Creates a new {@link ButtonComponentBarModel}
	 */
	public ButtonComponentBarModel(ButtonComponent buttonComponent) {
		_buttonComponent = buttonComponent;
	}

	@Override
	public List<CommandModel> getButtons() {
		return _buttonComponent.getButtons();
	}

	@Override
	public boolean isVisible() {
		return _buttonComponent.isVisible();
	}

	@Override
	public void addModelChangeListener(ModelChangeListener listener) {
		_buttonComponent.addModelChangeListener(listener);
	}

	@Override
	public void removeModelChangeListener(ModelChangeListener listener) {
		_buttonComponent.removeModelChangeListener(listener);
	}
}
