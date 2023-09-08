/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.ResourceProvider;

/**
 * {@link AbstractDispatchingResourceProvider} delegating constantly to a given
 * {@link ResourceProvider}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class ProxyResourceProvider extends AbstractDispatchingResourceProvider {

	private final ResourceProvider _inner;

	/**
	 * Creates a {@link ProxyResourceProvider} that uses the {@link MetaResourceProvider}, unless
	 * {@link #getProviderImpl(Object)} is overridden.
	 */
	public ProxyResourceProvider(ResourceProvider inner) {
		_inner = inner;
	}

	/**
	 * Creates a {@link ProxyResourceProvider} that uses the {@link MetaResourceProvider}, unless
	 * {@link #getProviderImpl(Object)} is overridden or the configured default is changed.
	 */
	public ProxyResourceProvider() {
		this(MetaResourceProvider.INSTANCE);
	}

	@Override
	protected ResourceProvider getProviderImpl(Object object) {
		return _inner;
	}

}
