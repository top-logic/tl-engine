/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.buttonbar;

import java.util.List;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.model.ModelChangeListener;

/**
 * Model of {@link ButtonBarControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface ButtonBarModel {

	/**
	 * list of buttons, which shall be shown in the button bar.
	 */
	List<CommandModel> getButtons();

	/**
	 * true, if the button bar is currently visible, false otherwise
	 */
	boolean isVisible();

	/**
	 * Adds a {@link ModelChangeListener} to this model, which will be informed, when some changes
	 * to this model occur.
	 */
	void addModelChangeListener(ModelChangeListener listener);

	/**
	 * @see ButtonBarModel#addModelChangeListener(ModelChangeListener)
	 */
	public void removeModelChangeListener(ModelChangeListener listener);
}
