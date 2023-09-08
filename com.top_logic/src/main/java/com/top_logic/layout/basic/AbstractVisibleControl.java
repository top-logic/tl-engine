/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

/**
 * {@link AbstractControl} that is always visible.
 * 
 * @since 5.7.4
 * 
 * @see AbstractConstantControl Constant control that is alway visible.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractVisibleControl extends AbstractControl {

	/**
	 * Creates a new {@link AbstractVisibleControl}.
	 * 
	 * @param commandsByName
	 *        see {@link AbstractControl#AbstractControl(Map)}.
	 */
	public AbstractVisibleControl(Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
	}

	/**
	 * Creates a new {@link AbstractVisibleControl}.
	 */
	public AbstractVisibleControl() {
		super();
	}

	/**
	 * Instances of {@link AbstractVisibleControl} are always visible, i.e. this method returns
	 * <code>true</code>.
	 * 
	 * @see com.top_logic.layout.View#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return true;
	}

}

