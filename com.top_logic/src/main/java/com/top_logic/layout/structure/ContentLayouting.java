/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link Layouting} rendering through {@link WriteBodyInline}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ContentLayouting extends AbstractContentLayouting<ContentLayouting.Config> {

	/**
	 * Configuration options for {@link ContentLayouting}.
	 */
	public interface Config extends AbstractContentLayouting.Config<ContentLayouting> {
		/* 
		 * Empty.
		 *
		 * This is necessary to prevent writing configuration defaults for content layouting when
		 * exporting layouts. See #26263. 
		 */
	}

	/**
	 * Creates a {@link ContentLayouting} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ContentLayouting(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment createContentDisplay(LayoutComponent component) {
		return new WriteBodyInline(component);
	}

}
