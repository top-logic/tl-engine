/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.securityObjectProvider.NullSecurityObjectProvider;
import com.top_logic.util.model.ModelService;

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
	 * Determines the collection of all types of possible security objects.
	 */
	default Set<TLClass> getPossibleSecurityObjectTypes() {
		return Collections.singleton(TLModelUtil.tlObjectType(ModelService.getApplicationModel()));
	}

	/**
	 * Instantiates the given {@link SecurityObjectProvider} configuration, never null.
	 */
	static SecurityObjectProvider fromConfiguration(InstantiationContext context,
			PolymorphicConfiguration<? extends SecurityObjectProvider> config) {
		SecurityObjectProvider result = fromConfigurationOptional(context, config);
		if (result == null) {
			return NullSecurityObjectProvider.INSTANCE;
		}
		return result;
	}

	/**
	 * Instantiates the given {@link SecurityObjectProvider} configuration.
	 */
	static SecurityObjectProvider fromConfigurationOptional(InstantiationContext context,
			PolymorphicConfiguration<? extends SecurityObjectProvider> config) {
		if (config instanceof ReferencedSecurityObjectProvider.Config ref) {
			// Short-cut.
			return SecurityObjectProviderManager.getInstance().getSecurityObjectProvider(ref.getReference());
		} else if (config != null) {
			return context.getInstance(config);
		} else {
			return null;
		}
	}

}
