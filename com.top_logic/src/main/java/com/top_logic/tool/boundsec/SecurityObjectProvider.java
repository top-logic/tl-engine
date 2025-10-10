/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.io.Serializable;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tool.boundsec.securityObjectProvider.NullSecurityObjectProvider;

/**
 * The DefaultSecurityObjectProvider provides the object at which the security should be
 * checked.
 * 
 * SecurityObjectProviders must be stateless as a single instance is cached by the 
 * {@link SecurityObjectProviderManager}
 * 
 * @extends {@link Serializable} in order to allow serialization of components
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface SecurityObjectProvider extends Serializable {

    /**
	 * Gets the object on which the security should be checked.
	 *
	 * @param aChecker
	 *        the bound checker which should check the security
	 * @param model
	 *        The model of the given {@link BoundChecker} or a potential model.
	 * @param aCommandGroup
	 *        the command group to get the security object for
	 * @return the object on which the security should be checked
	 */
	BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup);

	/**
	 * Instantiates the given {@link SecurityObjectProvider} configuration.
	 */
	static SecurityObjectProvider fromConfiguration(InstantiationContext context,
			PolymorphicConfiguration<? extends SecurityObjectProvider> config)
			throws ConfigurationException {
		if (config instanceof ReferencedSecurityObjectProvider.Config ref) {
			// Short-cut.
			return SecurityObjectProviderManager.getInstance().getSecurityObjectProvider(ref.getReference());
		} else if (config != null) {
			return context.getInstance(config);
		} else {
			return NullSecurityObjectProvider.INSTANCE;
		}
	}

}
