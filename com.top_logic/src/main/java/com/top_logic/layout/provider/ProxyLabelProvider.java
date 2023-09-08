/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;

/**
 * {@link LabelProvider} that delegates to an underlying {@link LabelProvider}
 * implementations.
 * 
 * <p>
 * This functionality can be useful, if a {@link ResourceProvider} should be
 * up-cast to the {@link LabelProvider} interface by dropping all functionality
 * but the {@link #getLabel(Object)} method.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ProxyLabelProvider implements LabelProvider {

	private final LabelProvider impl;

	/**
	 * Creates a new {@link ProxyLabelProvider} that delegates to the the given
	 * implementation.
	 */
	public ProxyLabelProvider(LabelProvider impl) {
		this.impl = impl;
	}
	
	@Override
	public String getLabel(Object object) {
		return impl.getLabel(object);
	}

}
