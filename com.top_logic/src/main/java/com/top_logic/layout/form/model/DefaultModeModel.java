/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

/**
 * Default mutable {@link ModeModel} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultModeModel extends AbstractModeModel {

	private int mode;
	
	public DefaultModeModel() {
		this(EDIT_MODE);
	}
	
	public DefaultModeModel(int initialMode) {
		this.mode = initialMode;
	}

	@Override
	public int getMode() {
		return this.mode;
	}
	
	/**
	 * Update the mode to the given value. 
	 */
	public void setMode(int newMode) {
		int oldMode = this.mode;
		
		this.mode = newMode;
		
		fireModeChange(oldMode, newMode);
	}

}
