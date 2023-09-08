/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dynamic;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link LayoutResolver}, that cannot resolve any layout.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DummyLayoutResolver extends LayoutResolver {

	/**
	 * Creates a {@link DummyLayoutResolver} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DummyLayoutResolver(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean canResolve(Object aModel) {
		return false;
	}

	@Override
	public String getLayoutName(Object aModel) {
		throw new UnsupportedOperationException();
	}

}
