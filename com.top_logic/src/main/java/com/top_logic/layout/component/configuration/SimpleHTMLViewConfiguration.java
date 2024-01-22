/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultView;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * The class {@link SimpleHTMLViewConfiguration} is a {@link ViewConfiguration} to write a single
 * HTML element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleHTMLViewConfiguration extends AbstractViewConfiguration<SimpleHTMLViewConfiguration.Config> {
	
	/**
	 * Configuration interface for {@link SimpleHTMLViewConfiguration}.
	 */
	public interface Config extends AbstractViewConfiguration.Config<SimpleHTMLViewConfiguration> {
		/** The configured tag. Default is <code>SPAN</code>. */
		@Name(XML_ATTRIBUTE_TAG)
		@StringDefault(HTMLConstants.SPAN)
		String getTag();

		/**
		 * The configured content key.
		 * 
		 * <p>
		 * Either {@link #getContentView()} or {@link #getContentKey()} must be set.
		 * </p>
		 */
		@Name(XML_ATTRIBUTE_CONTENT_KEY)
		@InstanceFormat
		ResKey getContentKey();

		/** The configured CSS class. */
		@Name(XML_ATTRIBUTE_CSS_CLASS)
		String getCssClass();

		/** The configured CSS style. */
		@Name(XML_ATTRIBUTE_STYLE)
		String getStyle();

		/**
		 * Inner view.
		 * 
		 * <p>
		 * Either {@link #getContentView()} or {@link #getContentKey()} must be set.
		 * </p>
		 */
		ViewConfiguration.Config<?> getContentView();
	}

	/**
	 * Configuration name for the value of the {@link SimpleHTMLViewConfiguration.Config#getTag()}.
	 */
	public static final String XML_ATTRIBUTE_TAG = "tag";

	/**
	 * Configuration name for the value of the
	 * {@link SimpleHTMLViewConfiguration.Config#getContentKey()}.
	 */
	public static final String XML_ATTRIBUTE_CONTENT_KEY = "contentKey";

	/**
	 * Configuration name for the value of the
	 * {@link SimpleHTMLViewConfiguration.Config#getCssClass()}.
	 */
	public static final String XML_ATTRIBUTE_CSS_CLASS = "cssClass";

	/**
	 * Configuration name for the value of the
	 * {@link SimpleHTMLViewConfiguration.Config#getStyle()}.
	 */
	public static final String XML_ATTRIBUTE_STYLE = "style";

	private ViewConfiguration _contentView;

	/**
	 * Creates a {@link SimpleHTMLViewConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public SimpleHTMLViewConfiguration(InstantiationContext context, SimpleHTMLViewConfiguration.Config config)
			throws ConfigurationException {
		super(context, config);
		if (config.getContentView() != null) {
			_contentView = context.getInstance(config.getContentView());
		} else if (config.getContentKey() == null) {
			context.error(
				Resources.getInstance().getString(I18NConstants.NO_CONTENT_GIVEN__VIEW_NAME.fill(config.getName())));
		}

	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		Config config = getConfig();
		HTMLFragment contentView;
		if (_contentView != null) {
			contentView = _contentView.createView(component);
		} else {
			contentView = Fragments.message(config.getContentKey());
		}
		String tag = config.getTag();
		String cssClass = StringServices.nonEmpty(config.getCssClass());
		String style = StringServices.nonEmpty(config.getStyle());

		return new DefaultView() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.beginBeginTag(tag);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
				out.writeAttribute(HTMLConstants.STYLE_ATTR, style);
				out.endBeginTag();
				contentView.write(context, out);
				out.endTag(tag);
			}
		};
	}
	
}

