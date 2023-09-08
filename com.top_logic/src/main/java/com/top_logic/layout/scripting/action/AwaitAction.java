/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.LongDefault;

/**
 * {@link ApplicationAction} configuration for actions that wait for asynchronous execution of other
 * actions.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AwaitAction extends ApplicationAction {

	/**
	 * Number of milliseconds to wait before aborting.
	 * 
	 * <p>
	 * A value of <code>&lt;=0</code> means not to wait at all.
	 * </p>
	 */
	@LongDefault(1000)
	long getMaxSleep();

	/**
	 * Sets the {@link #getMaxSleep()} property.
	 */
	void setMaxSleep(long value);

}

