/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util.model;

import com.top_logic.model.TLClass;

/**
 * Inheritance between two {@link TLClass}es.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface TLInheritance {

	/**
	 * Specialization of {@link #getGeneralization()}
	 */
	TLClass getSpecialization();

	/**
	 * Generalisation of {@link #getSpecialization()}
	 */
	TLClass getGeneralization();

}
