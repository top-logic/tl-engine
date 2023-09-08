/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ResourceRenderer;

/**
 * {@link ResourceProvider} that looks up its implementation from the {@link LabelProviderService}.
 * 
 * So it dispatches to configured ProxyResourceProviders. It is configured via the
 * "ResourceProviderRegistry" entry in the configuration file (top-logic.xml)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaResourceProvider extends AbstractDispatchingResourceProvider {

	public static final MetaResourceProvider INSTANCE = 
		new MetaResourceProvider();

    /** A Renderer based on the MetaResourceProvider.INSTANCE */
	public static final Renderer<Object> DEFAULT_RENDERER =
		ResourceRenderer.newResourceRenderer(INSTANCE);
	
	/**
	 * Singleton constructor.
	 */
	protected MetaResourceProvider() {
		super();
	}
	
	@Override
	protected ResourceProvider getProviderImpl(Object anObject) {
		return LabelProviderService.getInstance().getResourceProvider(anObject);
	}
	
}
