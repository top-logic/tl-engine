/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Resources;

/**
 * The renderer writes the {@link Version} of the product
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class DefaultVersionRenderer extends DefaultControlRenderer<Control>
		implements ConfiguredInstance<DefaultVersionRenderer.Config> {

	/**
	 * Configuration for {@link DefaultVersionRenderer}.
	 */
	public interface Config extends PolymorphicConfiguration<DefaultVersionRenderer> {
		/**
		 * See {@link #getEnvironment}.
		 */
		String ENVIRONMENT = "environment";

		/**
		 * See {@link #getShowEnvironment}.
		 */
		String SHOW_ENVIRONMENT = "showEnvironment";

		/** Information about the environment. */
		@Name(ENVIRONMENT)
		String getEnvironment();

		/**
		 * Whether to show the environment in the version field of the application, if the
		 * environment property is set.
		 */
		@Name(SHOW_ENVIRONMENT)
		boolean getShowEnvironment();
	}

	private static final String CSS_CLASS_FOOTER_STATUS = "FooterStatus";

	private static final String CSS_CLASS_FOOTER = "Footer";

	private static final ResKey TL_GENERAL_STATUSLINE_VERSION = I18NConstants.STATUS_BAR_VERSION;

	private final Config _config;

	/**
	 * Create a {@link DefaultVersionRenderer}.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to create the new object in.
	 * @param config
	 *        The configuration object to be used for instantiation.
	 */
	public DefaultVersionRenderer(InstantiationContext context, Config config) {
		_config = config;
	}

    @Override
    protected String getControlTag(Control control) {
        return DIV;
    }

    @Override
    protected void writeControlContents(DisplayContext context, TagWriter out, Control control) throws IOException {
        out.beginBeginTag(HTMLConstants.DIV);
        out.writeAttribute(HTMLConstants.CLASS_ATTR, CSS_CLASS_FOOTER_STATUS);
        out.endBeginTag();
		String theVersionString = getVersionString(Version.getApplicationVersion());
        Resources theResources = Resources.getInstance();
		String theString = theResources.getMessage(TL_GENERAL_STATUSLINE_VERSION, theVersionString);
        if (theString.startsWith("[")) {
            theString = theVersionString;
        }
        out.writeText(theString);
        out.endTag(HTMLConstants.DIV);
    }

	/**
	 * Gets the version as string.
	 */
    protected String getVersionString(Version aVersion) {
        StringBuilder sb = new StringBuilder();
		sb.append(aVersion.getVersionString());
		if (getShowEnvironment() && !StringServices.isEmpty(getEnvironment())) {
			sb.append(" (").append(getEnvironment()).append(")");
        }
        return sb.toString();
    }

	@Override
	public void appendControlCSSClasses(Appendable out, Control control) throws IOException {
		super.appendControlCSSClasses(out, control);
		HTMLUtil.appendCSSClass(out, CSS_CLASS_FOOTER);
	}

	/**
	 * The configuration this {@link DefaultVersionRenderer} is created with.
	 */
	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * Getter for {@link Config#getEnvironment()}.
	 */
	public String getEnvironment() {
		return getConfig().getEnvironment();
	}

	/**
	 * Getter for {@link Config#getShowEnvironment()}.
	 */
	public boolean getShowEnvironment() {
		return getConfig().getShowEnvironment();
	}

}
