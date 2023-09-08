/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The {@link MultiViewConfiguration} is a {@link ViewConfiguration} which wraps arbitrary views.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MultiViewConfiguration extends AbstractViewConfiguration<MultiViewConfiguration.Config> {

	private static final String XML_ATTRIBUTE_RENDERER = "renderer";
	private static final String XML_ATTRIBUTE_VIEWS = "views";
	
	/**
	 * Configuration interface for {@link MultiViewConfiguration}.
	 *
	 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
	 */
	public static interface Config extends AbstractViewConfiguration.Config<MultiViewConfiguration> {
		
		/** The configured renderer. */
		@Name(XML_ATTRIBUTE_RENDERER)
		@InstanceFormat
		@Mandatory
		ControlRenderer<? super BlockControl> getRenderer();
		
		/** The list of configured view configurations. */
		@Name(XML_ATTRIBUTE_VIEWS)
		List<ViewConfiguration.Config<ViewConfiguration>> getViews();
	}

	private final List<ViewConfiguration> innerViews;

	private ControlRenderer<? super BlockControl> renderer;

	/**
	 * Creates a {@link MultiViewConfiguration}.
	 */
	public MultiViewConfiguration(InstantiationContext context, MultiViewConfiguration.Config atts)
			throws ConfigurationException {
		super(context, atts);
		this.renderer = atts.getRenderer();
		this.innerViews = TypedConfiguration.getInstanceList(context, atts.getViews());
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		BlockControl view = new BlockControl();
		for (int index = 0, size = innerViews.size(); index < size; index++) {
			ViewConfiguration currentConfig = innerViews.get(index);
			HTMLFragment currentView = currentConfig.createView(component);
			view.addChild(currentView);
		}
		view.setRenderer(renderer);
		return view;
	}
	
}
