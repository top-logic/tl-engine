/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.HTMLFragmentView;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ViewConfiguration} creating an {@link ExternalLink}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExternalLinkViewConfiguration extends AbstractConfiguredInstance<ExternalLinkViewConfiguration.Config>
		implements ViewConfiguration, HTMLFragment {

	/**
	 * Configuration of an {@link ExternalLinkViewConfiguration}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ExternalLinkViewConfiguration> {

		/**
		 * The target side to open, e.g. http://www.top-logic.com.
		 */
		@Mandatory
		String getLink();

		/**
		 * The image to click to execute link.
		 */
		ThemeImage getImage();

		/**
		 * The link text.
		 */
		ResKey getLabelKey();

		/**
		 * An optional tool-tip for the link.
		 */
		ResKey getTooltipKey();

		/**
		 * The CSS class for the link.
		 */
		String getCSSClass();

		/**
		 * The {@link Renderer} to create the visual representation.
		 */
		@InstanceFormat
		@InstanceDefault(ToolRowCommandRenderer.class)
		Renderer<? super ExternalLink> getRenderer();
	}

	/**
	 * Creates a new {@link ExternalLinkViewConfiguration}.
	 */
	public ExternalLinkViewConfiguration(InstantiationContext context, ExternalLinkViewConfiguration.Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		return new HTMLFragmentView(this);
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		ExternalLink externalLink = new ExternalLink(getConfig().getLink());
		externalLink.setImage(getConfig().getImage());
		externalLink.setLabel(getConfig().getLabelKey());
		externalLink.setTooltip(getConfig().getTooltipKey());
		externalLink.setCSSClass(StringServices.nonEmpty(getConfig().getCSSClass()));
		getConfig().getRenderer().write(context, out, externalLink);
	}

}

