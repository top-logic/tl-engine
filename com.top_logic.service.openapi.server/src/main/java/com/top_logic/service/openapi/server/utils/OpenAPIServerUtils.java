/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.conf.PathItem;

/**
 * Utility class for {@link OpenApiServer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenAPIServerUtils {

	private static final Pattern PARAMETER_PATTERN = Pattern.compile(PathItem.VARIABLE_PATTERN);

	/**
	 * Determines the configuration of the {@link OpenApiServer}.
	 * 
	 * <p>
	 * This method can also be called when the {@link OpenApiServer} is not currently started.
	 * </p>
	 * 
	 * <p>
	 * When the {@link OpenApiServer} is started, the returned config may differ to the
	 * {@link OpenApiServer#getConfig()}
	 * </p>
	 * 
	 * @throws ConfigurationException
	 *         When accessing the stored configuration failed.
	 */
	public static OpenApiServer.Config<?> storedOpenAPIServerConfig() throws ConfigurationException {
		return (OpenApiServer.Config<?>) ApplicationConfig.getInstance()
				.getServiceConfiguration(OpenApiServer.class);
	}

	/**
	 * Determines the list of path parameters in the given path.
	 * 
	 * @param path
	 *        The path to analyse.
	 * @return Parameter names contained in the given path in the order of occurrence.
	 */
	public static List<String> containedParameters(String path) {
		List<String> parameters;
		Matcher matcher = PARAMETER_PATTERN.matcher(path);
		if (matcher.find()) {
			parameters = new ArrayList<>();
			do {
				parameters.add(matcher.group(1));
			} while (matcher.find());
		} else {
			parameters = Collections.emptyList();
		}
		return parameters;
	}

}

