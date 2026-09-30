/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Creates a {@link ResponseExtractor} accessing the status code of the response.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResponseStatusCode extends AbstractConfiguredInstance<ResponseStatusCode.Config<?>>
		implements ResponseExtractorFactory {

	/**
	 * Configuration options for {@link ResponseStatusCode}.
	 */
	@TagName("response-status-code")
	public interface Config<I extends ResponseStatusCode> extends PolymorphicConfiguration<I> {

		// No additional options

	}

	/**
	 * Creates a {@link ResponseStatusCode} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ResponseStatusCode(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ResponseExtractor newExtractor(MethodSpec method) {
		return (call, response) -> response.getCode();
	}

}
