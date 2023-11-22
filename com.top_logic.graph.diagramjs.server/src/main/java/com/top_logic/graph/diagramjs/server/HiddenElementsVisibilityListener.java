/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles the visibility of hidden elements. The user has the possibility to show elements that are
 * currently hidden in the diagram.
 * 
 * @see DiagramJSGraphComponent#showHiddenElements()
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public interface HiddenElementsVisibilityListener extends PropertyListener {

	/**
	 * Handles the hidden elements visibility.
	 * 
	 * @param sender
	 *        The control whose state had changed.
	 * @param oldValue
	 *        Whether hidden elements should be shown in the underlying diagram.
	 * @param newValue
	 *        Whether hidden elements should be shown in the underlying diagram.
	 * 
	 * @see HiddenElementsVisibilityListener
	 */
	void handleHiddenElementsVisibility(Object sender, Boolean oldValue, Boolean newValue);

}
