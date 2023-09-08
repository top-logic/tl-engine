/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dynamic;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * A LayoutResolver determines the layout to be used for a given object.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public abstract class LayoutResolver extends AbstractConfiguredInstance<LayoutResolver.Config> {

	/** Configuration of {@link LayoutResolver} */
	public interface Config extends PolymorphicConfiguration<LayoutResolver> {
		// No specific declaration
	}

	/**
	 * Creates a {@link LayoutResolver} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LayoutResolver(InstantiationContext context, Config config) {
		super(context, config);
	}

    /**
	 * Check, if this resolver can handle the given object
	 * 
	 * @param aModel
	 *        a model to resolve the layout for
	 * @return <code>true</code>, if the resolver can handle this object
	 */
	public abstract boolean canResolve(Object aModel);

	/**
	 * Description of the layout to use for the given model.
	 */
	public abstract String getLayoutName(Object aModel);

}
