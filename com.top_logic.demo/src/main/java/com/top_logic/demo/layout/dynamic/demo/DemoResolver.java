/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.dynamic.demo;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.dynamic.LayoutResolver;

/**
 * {@link LayoutResolver} for {@link DynamicLayoutSelectComponent}.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DemoResolver extends LayoutResolver {

	/**
	 * Creates a {@link DemoResolver} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DemoResolver(InstantiationContext context, Config config) {
		super(context, config);
	}

    @Override
	public boolean canResolve(Object aModel) {
		return aModel instanceof String;
    }

	@Override
	public String getLayoutName(Object aModel) {
		return (String) aModel;
	}

}

