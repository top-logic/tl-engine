/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Configuration of global key events.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface KeyEventConfiguration extends ConfigurationItem {

	/** Name of the configuration property {@link #getNoDefaultActionOnEnter()} */
	String NO_DEFAULT_ACTION_ON_ENTER = "no-default-action-on-enter";

	/**
	 * Whether the default action of the displayed {@link LayoutComponent} should <b>not</b> be
	 * executed when the user pressed "Enter" without any modifier, i.e. without "ctrl", "shift", or
	 * "alt".
	 * 
	 * @see LayoutComponent#getDefaultCommand()
	 */
	@Name(NO_DEFAULT_ACTION_ON_ENTER)
	@BooleanDefault(false)
	boolean getNoDefaultActionOnEnter();

}

