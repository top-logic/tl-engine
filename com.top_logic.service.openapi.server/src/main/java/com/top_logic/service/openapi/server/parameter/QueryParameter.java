/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.common.document.ParameterLocation;

/**
 * {@link RequestParameter} taken from the query arguments of the request URL.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ParameterUsedIn(ParameterLocation.QUERY)
public class QueryParameter extends ConcreteRequestParameter<QueryParameter.Config> {

	/**
	 * Configuration options for {@link QueryParameter}.
	 */
	@TagName("query-parameter")
	public interface Config extends ConcreteRequestParameter.Config<QueryParameter> {
		/**
		 * Whether multiple query arguments with the same name are allowed.
		 * 
		 * <p>
		 * The value of a parameter with option {@link #isMultiple()} is a list of values. If no
		 * query parameter of the specified name is given, an empty list is used as value.
		 * </p>
		 */
		@Override
		boolean isMultiple();
	}

	/**
	 * Creates a {@link QueryParameter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public QueryParameter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object getValue(HttpServletRequest req, Map<String, String> parametersRaw) throws InvalidValueException {
		if (getConfig().isMultiple()) {
			String[] rawValues = req.getParameterValues(getConfig().getName());
			if (rawValues == null) {
				checkNonMandatory();
				return Collections.emptyList();
			}
			ArrayList<Object> result = new ArrayList<>();
			for (String rawValue : rawValues) {
				result.add(parse(rawValue));
			}
			return result;
		} else {
			String rawValue = req.getParameter(getConfig().getName());
			if (rawValue == null) {
				checkNonMandatory();
				return null;
			}

			return parse(rawValue);
		}
	}

}
