/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.misc.AlwaysTrue;
import com.top_logic.service.openapi.common.document.ParameterLocation;
import com.top_logic.service.openapi.server.conf.Operation;

/**
 * {@link RequestParameter} taken from the path of the accessed URL.
 * 
 * @see Operation#getParameters()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ParameterUsedIn(ParameterLocation.PATH)
public class PathParameter extends ConcreteRequestParameter<PathParameter.Config> {

	/**
	 * Configuration options for {@link PathParameter}.
	 */
	@TagName("path-parameter")
	public interface Config extends ConcreteRequestParameter.Config<PathParameter> {

		/**
		 * Whether multiple path entries are consumed.
		 * 
		 * <p>
		 * The value of a parameter with option {@link #isMultiple()} is a list of values. A path
		 * parameter with option multiple must be the last path parameter in the list of parameters,
		 * since it consumes all path entries.
		 * </p>
		 */
		@Override
		boolean isMultiple();

		/**
		 * A path parameter is always required.
		 */
		@Override
		@Derived(fun = AlwaysTrue.class, args = {})
		boolean getRequired();

	}

	/**
	 * Creates a {@link PathParameter}.
	 */
	public PathParameter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object getValue(HttpServletRequest req, Map<String, String> parametersRaw) throws InvalidValueException {
		String variableValue = parametersRaw.get(getName());
		if (getConfig().isMultiple()) {
			ArrayList<Object> result = new ArrayList<>();
			int index = 0;
			while (true) {
				int separator = variableValue.indexOf('/', index);
				if (separator == index) {
					// ignore empty parts.
					index = separator + 1;
				} else if (separator > -1) {
					result.add(parse(variableValue.substring(index, separator)));
					index = separator + 1;
				} else {
					result.add(parse(variableValue.substring(index)));
					break;
				}
			}
			if (result.isEmpty()) {
				checkNonMandatory();
			}
			return result;
		} else {
			if (variableValue == null) {
				return null;
			}

			return parse(variableValue);
		}
	}

}
