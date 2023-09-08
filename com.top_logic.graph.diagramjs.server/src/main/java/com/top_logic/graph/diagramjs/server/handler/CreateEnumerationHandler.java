/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.handler;

import com.top_logic.graph.diagramjs.server.util.layout.Bounds;
import com.top_logic.model.TLEnumeration;

/**
 * Handler to create a {@link TLEnumeration} node.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface CreateEnumerationHandler {

	/**
	 * @see CreateEnumerationHandler
	 */
	void createEnumeration(Bounds bounds);
}
