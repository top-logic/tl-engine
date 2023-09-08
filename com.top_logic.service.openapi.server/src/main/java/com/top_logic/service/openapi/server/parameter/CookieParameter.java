/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.common.document.ParameterLocation;

/**
 * Parameter that must be given as cookie value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ParameterUsedIn(ParameterLocation.COOKIE)
public class CookieParameter extends ConcreteRequestParameter<CookieParameter.Config> {

	/**
	 * Configuration options for {@link CookieParameter}.
	 */
	@TagName("cookie-parameter")
	public interface Config extends ConcreteRequestParameter.Config<CookieParameter> {

		// no special configuration here.

	}

	/**
	 * Creates a new {@link CookieParameter}.
	 */
	public CookieParameter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object getValue(HttpServletRequest req, Map<String, String> parametersRaw) throws InvalidValueException {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) {
			checkNonMandatory();
			return null;
		}
		String cookieName = getName();
		if (getConfig().isMultiple()) {
			ArrayList<Object> result = new ArrayList<>();
			for (Cookie cookie : cookies) {
				if (cookieName.equals(cookie.getName())) {
					result.add(parse(cookie.getValue()));
				}
			}
			if (result.isEmpty()) {
				checkNonMandatory();
				return Collections.emptyList();
			}
			return result;
		} else {
			for (Cookie cookie : cookies) {
				if (cookieName.equals(cookie.getName())) {
					return parse(cookie.getValue());
				}
			}
			checkNonMandatory();
			return null;
		}
	}

}

