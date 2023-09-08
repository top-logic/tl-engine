/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.handler;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;

/**
 * Handler to create a reference between {@link TLClass}es.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface CreateReferenceHandler {

	/**
	 * Creates the reference with the given properties.
	 */
	void createReference(String type, TLType source, TLType target);
}
