/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.util.Resources;

/**
 * {@link BoundComponent} that simply displays an internationalized message in its body.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SimpleBoundComponent extends BoundComponent {

	/**
	 * Configuration options of {@link SimpleBoundComponent}.
	 */
	public interface Config extends BoundComponent.Config {
		/**
		 * Resource message to display in the component body.
		 */
		@Name("content-message")
		@InstanceFormat
		ResKey getContentMessage();

		/** @see #getContentMessage() */
		void setContentMessage(ResKey content);
	}

	private final ResKey _content;

	/**
	 * Creates a {@link SimpleBoundComponent} from configuration.
	 */
	public SimpleBoundComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_content = config.getContentMessage();
	}

	@Override
	public void writeBody(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TagWriter out)
			throws IOException, ServletException {
		HTMLUtil.beginDiv(out, FormConstants.FORM_BODY_CSS_CLASS);
		{
			out.writeContent(Resources.getInstance().getString(_content));
		}
		HTMLUtil.endDiv(out);
	}

}
