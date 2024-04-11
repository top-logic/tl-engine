/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout.views;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.I18NConstants;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.DefaultView;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.themes.modern.layout.ModernThemeConstants;
import com.top_logic.util.Resources;

/**
 * {@link DefaultComponentView}, which displays a title bar of the application window.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TitleView extends DefaultComponentView implements ModernThemeConstants, HTMLConstants {

	private ViewConfiguration _logoutConfig;

	/**
	 * Configuration interface of {@link TitleView}
	 */
	public interface Config extends PolymorphicConfiguration<ViewConfiguration> {

		/**
		 * {@link ViewConfiguration}, which describes a {@link View} of an application
		 *         logout command.
		 */
		PolymorphicConfiguration<? extends ViewConfiguration> getLogoutView();
	}

	/**
	 * Creates a {@link TitleView} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TitleView(InstantiationContext context, Config config) {
		_logoutConfig = context.getInstance(config.getLogoutView());
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		return new TitleViewImpl(toView(component, _logoutConfig));
	}

	static class TitleViewImpl extends DefaultView {

		private static final ResKey HEADER_TITLE = I18NConstants.APPLICATION_TITLE;

		private HTMLFragment _logoutView;

		/**
		 * Creates a {@link TitleViewImpl}.
		 */
		public TitleViewImpl(HTMLFragment logoutView) {
			_logoutView = logoutView;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			beginDiv(out, CSS_CLASS_TITLE_TOP, true);
			beginDiv(out, CSS_CLASS_TITLE_NAME, true);
			beginDiv(out, CSS_CLASS_CLOSE, true);
			_logoutView.write(context, out);
			out.endTag(DIV);
			out.writeText(Resources.getInstance().getString(HEADER_TITLE));
			out.endTag(DIV);
			out.endTag(DIV);
		}

		private void beginDiv(TagWriter out, String aClass, boolean endTagBegin) {
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, aClass);
			if (endTagBegin) {
				out.endBeginTag();
			}
		}
	}
}
