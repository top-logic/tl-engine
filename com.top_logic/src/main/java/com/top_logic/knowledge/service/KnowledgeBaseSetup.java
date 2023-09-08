/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Arrays;
import java.util.Collection;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.tool.boundsec.manager.AccessManager;

/**
 * {@link DBSetupAction} that manages table creation for the {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class KnowledgeBaseSetup implements DBSetupAction {

	@Override
	public Collection<? extends BasicRuntimeModule<?>> getDependencies() {
		return Arrays.asList(dependencies());
	}

	private static BasicRuntimeModule<?>[] dependencies() {
		return new BasicRuntimeModule<?>[] {
			FlexDataManagerFactory.Module.INSTANCE,
			DataAccessService.Module.INSTANCE };
	}

	/**
	 * The optional dependencies for starting a {@link KnowledgeBase}.
	 * 
	 */
	public static BasicRuntimeModule<?>[] getOptionalDependencies() {
		return new BasicRuntimeModule<?>[] {
			/* The AccessManager is an optional dependency of the KnowledgeBase. It updates the
			 * security on commit. Without the AccessManager, the security is not updated and has to
			 * be completely rebuild later on. */
			AccessManager.Module.INSTANCE
		};
	}

}
