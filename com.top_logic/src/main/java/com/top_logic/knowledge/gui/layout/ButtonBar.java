/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.buttonbar.ButtonBarModel;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Button bar typically rendered below some content component in a dialog.
 * 
 * @see LayoutComponent#getButtonBar()
 */
public class ButtonBar extends PropertyObservableBase implements ButtonBarModel {

	/**
	 * Button bar CSS class.
	 */
	public static final String DEFAULT_CSS_CLASS = "cmdButtons";
	
    /**
	 * Property of a {@link CommandModel} giving a {@link ControlProvider} to render the
	 * corresponding button.
	 */
	public static final Property<ControlProvider> BUTTON_CONTROL_PROVIDER =
		TypedAnnotatable.property(ControlProvider.class, "buttonControlProvider");

	/**
	 * Buttons to display.
	 */     
	private List<CommandModel> _buttons;
    
    /**
	 * The command model representing the given command handler.
	 */
    public CommandModel getCommandModel(CommandHandler aCommand) {
		for (int i = 0, size = _buttons.size(); i < size; i++) {
			Object theModel = _buttons.get(i);

            if ((theModel instanceof ComponentCommandModel) && (aCommand == ((ComponentCommandModel) theModel).getCommandHandler())) {
                return (CommandModel) theModel;
            }
        }

        return null;
    }

    /**
	 * Updates the list of displayed buttons.
	 */
    public void setButtons(List<? extends CommandModel> buttons) {
		if (_buttons == null) {
            if (buttons != null) {
				_buttons = new ArrayList<>(buttons);
            }
        } else {
			_buttons.clear();
            if (buttons != null) {
				_buttons.addAll(buttons);
            }
        }
		fireButtonsChanged();
    }

    /**
	 * Adds the given buttons to the list of displayed buttons.
	 */
    public void addButtons(List<? extends CommandModel> buttons) {
		if (_buttons == null) {
            if (buttons != null) {
				_buttons = new ArrayList<>(buttons);
            }
        } else {
            if (buttons != null) {
				_buttons.addAll(buttons);
            }
        }
		fireButtonsChanged();
    }

	private void fireButtonsChanged() {
		notifyListeners(ModelChangeListener.MODEL_CHANGED, this, null, _buttons);
	}

    /**
     * Return the List of (transient) Buttons we show.
     * 
     * The transient Buttons will be used only in case the normal Buttons
     * are null (for now).
     */
	@Override
	public List<CommandModel> getButtons() {
		return this._buttons;
    }

	@Override
	public boolean isVisible() {
		return !_buttons.isEmpty();
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