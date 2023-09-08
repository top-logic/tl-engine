/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ViewConfiguration}, which can be used to define a view, which triggers
 * {@link GuiInspectorCommand}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class OpenGuiInspectorView extends AbstractViewConfiguration<OpenGuiInspectorView.Config> {

	private final View openGuiInspectorView;

	/**
	 * Configuration options for {@link OpenGuiInspectorView}
	 */
	public interface Config extends AbstractViewConfiguration.Config<OpenGuiInspectorView> {

		/** @see #getImage() */
		String PROPERTY_IMAGE = "image";

		/** @see #getLabelKey() */
		String PROPERTY_LABEL_KEY = "labelKey";

		/**
		 * The icon to display.
		 */
		@Name(PROPERTY_IMAGE)
		@NonNullable
		@Mandatory
		ThemeImage getImage();

		/**
		 * The label {@link ResKey} of the command.
		 */
		@Name(PROPERTY_LABEL_KEY)
		@InstanceFormat
		@NonNullable
		@Mandatory
		ResKey getLabelKey();

		/**
		 * The {@link Renderer} to create the visual representation.
		 */
		@InstanceFormat
		@InstanceDefault(ToolRowCommandRenderer.class)
		Renderer<? super OpenGuiInspectorFragment> getRenderer();
	}

	/**
	 * Creates a {@link OpenGuiInspectorView} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OpenGuiInspectorView(InstantiationContext context, OpenGuiInspectorView.Config config)
			throws ConfigurationException {
		super(context, config);
		openGuiInspectorView =
			new OpenGuiInspectorFragment(config.getRenderer(), config.getImage(), config.getLabelKey());
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		return openGuiInspectorView;
	}

}
