/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

import com.top_logic.layout.View;

/**
 * {@link AbstractConstantControl} is an {@link AbstractConstantControlBase} that is always visible.
 * 
 * @since 5.7.3
 * 
 * @see AbstractVisibleControl Subclass of {@link AbstractControl} that is always visible.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractConstantControl extends AbstractConstantControlBase {

	/**
	 * Creates a new {@link AbstractConstantControl}.
	 * 
	 * @see AbstractConstantControlBase#AbstractConstantControlBase(Map)
	 */
	public AbstractConstantControl(Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
	}

	/**
	 * Creates a new {@link AbstractConstantControl}.
	 * 
	 * @see AbstractConstantControlBase#AbstractConstantControlBase()
	 */
	public AbstractConstantControl() {
	}

	@Override
	public Object getModel() {
		return null;
	}

	/**
	 * This control is always visible
	 * 
	 * @see View#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return true;
	}

}

