/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.gui.layout.HttpSecureHeaderFilter.GlobalConfig.Header;

/**
 * {@link Filter} setting common HTTP headers enhancing app security.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HttpSecureHeaderFilter implements Filter {

	/**
	 * HTTP header for controlling the embedding to the application in frames.
	 */
	public static final String X_FRAME_OPTIONS = "X-Frame-Options";

	/**
	 * HTTP header for requesting HTTPS, if the application can be accessed through HTTPS.
	 */
	public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

	/**
	 * HTTP enabling browser heuristics for XSS protection.
	 */
	public static final String X_XSS_PROTECTION = "X-XSS-Protection";

	/**
	 * HTTP header disabling content sniffing.
	 */
	public static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

	/**
	 * Configuration options for {@link HttpSecureHeaderFilter}.
	 */
	public interface GlobalConfig extends ConfigurationItem {
		/**
		 * Header values to set on each response.
		 */
		@Name("headers")
		@Key(Header.NAME)
		List<Header> getHeaders();

		/**
		 * A HTTP header to set.
		 */
		interface Header extends ConfigurationItem {
			/**
			 * @see #getName()
			 */
			String NAME = "name";

			/**
			 * @see #getValues()
			 */
			String VALUES = "values";

			/**
			 * Name of the header to set.
			 */
			@Name(NAME)
			@Mandatory
			String getName();

			/**
			 * Values for the header to set.
			 */
			@Name(VALUES)
			@Mandatory
			@Format(CommaSeparatedStrings.class)
			@ListBinding(tag = "value", attribute = "value")
			List<String> getValues();
		}
	}

	private List<Consumer<HttpServletResponse>> _headers;

	@Override
	public void init(FilterConfig config) throws ServletException {
		GlobalConfig appConfig = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);

		_headers = new ArrayList<>();
		for (Header header : appConfig.getHeaders()) {
			String name = header.getName();
			for (String value : header.getValues()) {
				Logger.info(name + ": " + value, HttpSecureHeaderFilter.class);

				_headers.add(response -> response.addHeader(name, value));
			}
		}
	}

	@Override
	public void destroy() {
		// Ignore.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (response instanceof HttpServletResponse) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			for (Consumer<HttpServletResponse> header : _headers) {
				header.accept(httpResponse);
			}
		}

		chain.doFilter(request, response);
	}

}
