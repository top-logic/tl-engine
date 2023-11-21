/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.handler;

import java.util.Collection;

/**
 * Handler to handle diagram graph parts visibility changes.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public interface ElementsVisibilityHandler {

	/**
	 * @see ElementsVisibilityHandler
	 */
	void setElementsVisibility(Collection<Object> graphPartModels, boolean isVisible);

}
