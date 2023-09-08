/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.buttonbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.model.ModelChangeListener;

/**
 * {@link ButtonBarModel}, that is solely based on a list of {@link CommandModel}s, which shall be
 * displayed in the button bar.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SimpleButtonBarModel extends PropertyObservableBase implements ButtonBarModel {

	private List<CommandModel> _buttons;

	/**
	 * Creates a {@link SimpleButtonBarModel}.
	 */
	public SimpleButtonBarModel() {
		_buttons = Collections.emptyList();
	}

	/**
	 * Creates a {@link SimpleButtonBarModel}
	 * 
	 * @param buttons
	 *        The initial buttons to display.
	 */
	public SimpleButtonBarModel(List<CommandModel> buttons) {
		_buttons = Collections.unmodifiableList(new ArrayList<>(buttons));
	}

	/**
	 * Updates the buttons in this {@link ButtonBarModel}
	 * 
	 * @param buttons
	 *        The new list of {@link CommandModel}s to show.
	 */
	public void setButtons(List<CommandModel> buttons) {
		List<CommandModel> oldButtons = _buttons;
		ArrayList<CommandModel> newButtons = new ArrayList<>(buttons);
		_buttons = Collections.unmodifiableList(newButtons);
		fireModelChanged(oldButtons);
	}

	/**
	 * Adds the given buttons to the list of displayed buttons.
	 * 
	 * @param buttons
	 *        The {@link CommandModel} to append to the buttons in this {@link ButtonBarModel}.
	 */
	public void addButtons(List<CommandModel> buttons) {
		List<CommandModel> oldButtons = _buttons;
		ArrayList<CommandModel> newButtons = new ArrayList<>(_buttons.size() + buttons.size());
		newButtons.addAll(oldButtons);
		newButtons.addAll(buttons);
		_buttons = Collections.unmodifiableList(newButtons);
		fireModelChanged(oldButtons);
	}

	/**
	 * Removes all buttons from this {@link ButtonBarModel}.
	 * 
	 * @see #addButtons(List)
	 */
	public void clear() {
		List<CommandModel> oldButtons = _buttons;
		_buttons = Collections.emptyList();
		fireModelChanged(oldButtons);
	}

	private void fireModelChanged(List<CommandModel> oldButtons) {
		notifyListeners(ModelChangeListener.MODEL_CHANGED, this, oldButtons, _buttons);
	}

	@Override
	public List<CommandModel> getButtons() {
		return _buttons;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void addModelChangeListener(ModelChangeListener listener) {
		addListener(ModelChangeListener.MODEL_CHANGED, listener);
	}

	@Override
	public void removeModelChangeListener(ModelChangeListener listener) {
		removeListener(ModelChangeListener.MODEL_CHANGED, listener);
	}

}
