/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles the display of hidden elements.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public interface DisplayHiddenElementsListener extends PropertyListener {

	/**
	 * Handles the display of hidden elements.
	 */
	void handleDisplayHiddenElements(Object sender, Boolean oldValue, Boolean newValue);

}
