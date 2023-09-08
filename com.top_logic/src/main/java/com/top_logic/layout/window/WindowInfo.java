/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.window;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.mig.html.layout.AbstractWindowInfo;

/**
 * Display properties of a window.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WindowInfo extends AbstractWindowInfo {

	/** @see #getMultipleWindows() */
	String MULTI_OPEN = "multiWindow";

	/** @see #getCloseIfParentBecomesInvisible() */
	String CLOSE_IF_PARENT_BECOMES_INVISIBLE = "closeIfParentBecomesInvisible";

	@Override
	@ClassDefault(OpenWindowCommand.class)
	Class<? extends OpenWindowCommand> getOpenHandlerClass();

	/**
	 * Whether the window can be opened multiple times.
	 * 
	 * <p>
	 * If <code>false</code>, an existing window is reused for following open requests.
	 * </p>
	 */
	@Name(MULTI_OPEN)
	@BooleanDefault(false)
	boolean getMultipleWindows();

	/**
	 * Whether the window should be closed when its opener becomes invisible.
	 */
	@Name(CLOSE_IF_PARENT_BECOMES_INVISIBLE)
	@BooleanDefault(false)
	boolean getCloseIfParentBecomesInvisible();

	@Override
	@NullDefault
	@ImplementationClassDefault(OpenWindowCommand.class)
	OpenWindowCommand.Config getOpenHandler();

}