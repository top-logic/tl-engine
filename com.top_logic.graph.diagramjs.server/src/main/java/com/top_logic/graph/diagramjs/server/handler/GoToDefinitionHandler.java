/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.handler;

import com.top_logic.model.TLModelPart;

/**
 * Handler to go to the diagram definition of a given {@link TLModelPart}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface GoToDefinitionHandler {

	/**
	 * @see GoToDefinitionHandler
	 */
	void gotoDefinition(TLModelPart modelPart);

}
