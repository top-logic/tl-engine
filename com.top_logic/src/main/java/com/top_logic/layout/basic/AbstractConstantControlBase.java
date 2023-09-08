/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;

/**
 * {@link AbstractControlBase} which has no updates.
 * 
 * @see AbstractConstantControl Subclass that is always visible.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractConstantControlBase extends AbstractControlBase {

	/**
	 * Creates a {@link AbstractConstantControlBase}.
	 * 
	 * @param commandsByName
	 *        the commands which can be executed in this control
	 * 
	 * @see AbstractControlBase#AbstractControlBase(Map)
	 */
	protected AbstractConstantControlBase(Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
	}

	/**
	 * Creates a {@link AbstractConstantControlBase}.
	 * 
	 */
	protected AbstractConstantControlBase() {
		super();
	}

	/**
	 * Control has no updates.
	 * 
	 * @see AbstractConstantControlBase#internalRevalidate(DisplayContext, UpdateQueue)
	 * @see AbstractControlBase#hasUpdates()
	 */
	@Override
	protected boolean hasUpdates() {
		return false;
	}

	/**
	 * No revalidation as there are no updates.
	 * 
	 * @see AbstractConstantControlBase#hasUpdates()
	 * @see AbstractControlBase#internalRevalidate(DisplayContext, UpdateQueue)
	 */
	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// no updates; no revalidation
	}

}

