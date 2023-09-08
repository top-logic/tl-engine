/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} that embeds any legacy {@link LayoutComponent}
 * through an {@link InlineContentView} in div layout.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InlineLayoutControlProvider extends ContentLayoutControlProvider<InlineLayoutControlProvider.Config> {
	
	/**
	 * Configuration options for {@link InlineLayoutControlProvider}.
	 */
	public interface Config extends ContentLayoutControlProvider.Config<InlineLayoutControlProvider> {

		/**
		 * Optional reference to JSP page to render.
		 * 
		 * <p>
		 * If this option is given, the default rendering defined by the component does not apply.
		 * Instead, the given JSP page is rendered in the context of the
		 * {@link InlineLayoutControlProvider#mkLayout(com.top_logic.layout.structure.LayoutControlProvider.Strategy, LayoutComponent)
		 * component}.
		 * </p>
		 */
		@Nullable
		String getPage();

	}

	/**
	 * Singleton {@link InlineLayoutControlProvider} instance.
	 */
	public static final LayoutControlProvider INSTANCE = new InlineLayoutControlProvider();

	private final String _page;

	private InlineLayoutControlProvider() {
		this(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, TypedConfiguration.newConfigItem(Config.class));
	}
	
	/**
	 * Creates a {@link InlineLayoutControlProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InlineLayoutControlProvider(InstantiationContext context, Config config) {
		super(context, config);
		_page = config.getPage();
	}
	
	@Override
	protected HTMLFragment createView(LayoutComponent component) {
		return _page == null ? new WriteBodyInline(component) : new InlineJSPView(component, _page);
	}

}
