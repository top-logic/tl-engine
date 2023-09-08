/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.schema.setup.config;


import com.top_logic.basic.Log;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.meta.MORepository;

/**
 * Provides types for the persistency layer of an application.
 * 
 * @see ApplicationTypes
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeProvider {

	/**
	 * Create required types in the given {@link MORepository}.
	 * 
	 * @param log
	 *        The {@link Log} to report errors to.
	 * @param typeFactory
	 *        The {@link MOFactory} that should be used to instantiate types.
	 * @param typeRepository
	 *        The modified repository.
	 */
	void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository);

}
