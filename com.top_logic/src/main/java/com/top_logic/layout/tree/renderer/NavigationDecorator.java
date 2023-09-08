/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ContentDecorator;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.TLContext;

/**
 * The class {@link NavigationDecorator} can be used to render the name of the current user
 * additionally.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NavigationDecorator extends AbstractConfiguredInstance<NavigationDecorator.Config>
		implements ContentDecorator, HTMLConstants {

	/**
	 * Configuration options for {@link NavigationDecorator}.
	 */
	public interface Config extends PolymorphicConfiguration<ContentDecorator> {
		/**
		 * Whether to display a logout count-down.
		 */
		@BooleanDefault(true)
		@Name("logoutTimer")
		boolean getLogoutTimer();

		/**
		 * If the logout view is set it will be displayed.
		 */
		@Name("logoutView")
		PolymorphicConfiguration<? extends View> getLogoutView();
	}

	/**
	 * Creates a {@link NavigationDecorator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NavigationDecorator(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void startDecoration(DisplayContext context, TagWriter out, Object value) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "Header");
		out.endBeginTag();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "BreadCrumbOuter");
		out.endBeginTag();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "Left");
		out.endBeginTag();
	}

	@Override
	public void endDecoration(DisplayContext context, TagWriter out, Object value) throws IOException {
		out.endTag(DIV);

		{
			if (getConfig().getLogoutView() != null) {
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, "Right");
				out.endBeginTag();
				TypedConfigUtil.createInstance(getConfig().getLogoutView()).write(context, out);
				out.endTag(DIV);
			}

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, "Right");
			out.endBeginTag();
			TLContext theContext = TLContext.getContext();
			out.writeText(theContext.getCurrentPersonWrapper().getFullName());
			out.endTag(DIV);

			if (getConfig().getLogoutTimer()) {
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, "Right");
				out.endBeginTag();
				new LogoutTimerControl().write(context, out);
				out.endTag(DIV);
			}
		}
		out.endTag(DIV);

		out.endTag(DIV);
	}

}
