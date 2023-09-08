/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.AbstractMappingResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * The class {@link TLTreeNodeResourceProvider} is a {@link ResourceProvider}
 * for {@link TLTreeNode}.
 * 
 * If some method is called using a {@link TLTreeNode}, the call is dispatched
 * to its implementation using {@link TLTreeNode#getBusinessObject() the user
 * object} of the node.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLTreeNodeResourceProvider
		extends AbstractMappingResourceProvider<AbstractMappingResourceProvider.Config<?>> {

	/**
	 * Singleton {@link TLTreeNodeResourceProvider} instance.
	 */
	public static final TLTreeNodeResourceProvider INSTANCE =
		newTLTreeNodeResourceProvider(MetaResourceProvider.INSTANCE);

	/**
	 * Creates a {@link TLTreeNodeResourceProvider}.
	 * 
	 * @param userObjectResourceProvider
	 *        {@link ResourceProvider} to delegate to.
	 */
	public static TLTreeNodeResourceProvider newTLTreeNodeResourceProvider(
			ResourceProvider userObjectResourceProvider) {
		Config<TLTreeNodeResourceProvider> config;
		try {
			config =
				(Config<TLTreeNodeResourceProvider>) TypedConfiguration
					.createConfigItemForImplementationClass(TLTreeNodeResourceProvider.class);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		config.setTargetResourceProvider(userObjectResourceProvider);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * Creates a {@link TLTreeNodeResourceProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLTreeNodeResourceProvider(InstantiationContext context, Config<?> config) {
        super(context, config);
    }

	@Override
	protected Object mapValue(Object anObject) {
		return ((TLTreeNode<?>) anObject).getBusinessObject();
	}

}
