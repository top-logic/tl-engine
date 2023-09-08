/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.common.document.ParameterLocation;

/**
 * Parameter that is expected to be present as header value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ParameterUsedIn(ParameterLocation.HEADER)
public class HeaderParameter extends ConcreteRequestParameter<HeaderParameter.Config> {

	/**
	 * Configuration options for {@link HeaderParameter}.
	 */
	@TagName("header-parameter")
	public interface Config extends ConcreteRequestParameter.Config<HeaderParameter> {

		// no special configuration here.

	}

	/**
	 * Creates a new {@link HeaderParameter}.
	 */
	public HeaderParameter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object getValue(HttpServletRequest req, Map<String, String> parametersRaw) throws InvalidValueException {
		if (getConfig().isMultiple()) {
			Enumeration<String> headers = req.getHeaders(getConfig().getName());
			if (!headers.hasMoreElements()) {
				checkNonMandatory();
				return Collections.emptyList();
			}
			ArrayList<Object> result = new ArrayList<>();
			do {
				result.add(parse(headers.nextElement()));
			} while (headers.hasMoreElements());
			return result;
		} else {
			String header = req.getHeader(getConfig().getName());
			if (header == null) {
				checkNonMandatory();
				return null;
			}
			return parse(header);
		}
	}

}

