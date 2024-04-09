/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} for observing {@link ToolBar#canMaximize()},
 * {@link ToolBar#showMaximize()}, {@link ToolBar#showMinimize()}, and {@link ToolBar#showPopOut()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ToolbarOptionsListener extends PropertyListener {

	/**
	 * Callback informing that some of the {@link ToolBar} options have changed.
	 * 
	 * @param sender
	 *        The modified {@link ToolBar}.
	 */
	void notifyToolbarOptionsChanged(ToolBar sender);

}
