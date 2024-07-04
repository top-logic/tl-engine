/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.embedd;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.xml.LayoutControlComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkService;

/**
 * {@link LayoutControlProvider} for e.g. a {@link LayoutControlComponent} to embedd a
 * micro-frontend of another application via an <code>iframe</code> element.
 */
public class EmbeddedViewControlProvider extends AbstractConfiguredInstance<EmbeddedViewControlProvider.Config<?>>
		implements LayoutControlProvider {

	/**
	 * Configuration options for {@link EmbeddedViewControlProvider}.
	 */
	public interface Config<I extends EmbeddedViewControlProvider>
			extends PolymorphicConfiguration<I>, WithPostCreateActions.Config {
		/**
		 * Algorithm constructing the URL to display.
		 */
		@Name("urlProvider")
		PolymorphicConfiguration<? extends URLProvider> getUrlProvider();
	}

	private URLProvider _urlProvider;
	private List<PostCreateAction> _actions;

	/**
	 * Creates a {@link EmbeddedViewControlProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public EmbeddedViewControlProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
		_urlProvider = context.getInstance(config.getUrlProvider());
		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
		UICallback onClose = (commandContext, args) -> {
			Object resultObject = args.isEmpty() ? null : BookmarkService.getInstance().resolveBookmark(args);
			WithPostCreateActions.processCreateActions(_actions, component, resultObject);
			return HandlerResult.DEFAULT_RESULT;
		};

		return new EmbeddedView(component, _urlProvider, onClose);
	}

}
