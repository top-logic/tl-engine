/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.ParameterLocation;

/**
 * Interprets the request body as parameter.
 * 
 * <p>
 * This allows to read the resource contents e.g. in a {@link HttpMethod#PUT} request from the
 * method body.
 * </p>
 *
 * @see MultiPartBodyParameter
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ParameterUsedIn(ParameterLocation.QUERY)
public class RequestBodyParameter extends ConcreteRequestParameter<RequestBodyParameter.Config> {

	/**
	 * Configuration options for {@link RequestBodyParameter}.
	 */
	@DisplayOrder({
		Config.NAME_ATTRIBUTE,
		Config.DESCRIPTION,
		Config.FORMAT,
		Config.REQUIRED,
		Config.SCHEMA,
		Config.EXAMPLE,
	})
	@DisplayInherited(DisplayStrategy.IGNORE)
	@TagName("request-body")
	public interface Config extends ConcreteRequestParameter.Config<RequestBodyParameter> {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * There is only one body.
		 */
		@Override
		@Hidden
		@Derived(fun = AlwaysFalse.class, args = {})
		boolean isMultiple();

		@Override
		default boolean isBodyParameter() {
			return true;
		}
	}

	/**
	 * Creates a {@link RequestBodyParameter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RequestBodyParameter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object getValue(HttpServletRequest req, Map<String, String> parametersRaw) throws InvalidValueException {
		try {
			BufferedReader in = req.getReader();

			StringBuilder contents = new StringBuilder();
			char[] buffer = new char[4096];
			while (true) {
				int direct = in.read(buffer);
				if (direct < 0) {
					break;
				}

				contents.append(buffer, 0, direct);
			}

			Object result = parse(contents.toString());
			if (result == null) {
				checkNonMandatory();
			}
			return result;
		} catch (IOException ex) {
			throw new InvalidValueException("Failed to read body data.", ex);
		}
	}
}
