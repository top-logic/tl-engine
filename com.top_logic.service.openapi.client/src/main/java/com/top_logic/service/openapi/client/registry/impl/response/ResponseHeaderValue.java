/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ProtocolException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Creates a {@link ResponseExtractor} accessing the header (resp. headers) of the response with a
 * certain name.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResponseHeaderValue extends AbstractConfiguredInstance<ResponseHeaderValue.Config<?>>
		implements ResponseExtractorFactory {

	/**
	 * Configuration options for {@link ResponseHeaderValue}.
	 */
	@TagName("header-value")
	public interface Config<I extends ResponseHeaderValue> extends PolymorphicConfiguration<I> {

		/** Configuration option for {@link #getHeader()} */
		String HEADER_NAME = "header";

		/** Configuration option for {@link #isMultiple()} */
		String MULTIPLE = "multiple";

		/**
		 * Name of the header to take the value from.
		 */
		@Name(HEADER_NAME)
		@Mandatory
		String getHeader();

		/**
		 * Setter for {@link #getHeader()}.
		 */
		void setHeader(String value);

		/**
		 * Whether all headers with name {@link #getHeader()} must be reported. In this case the
		 * value is a list of strings.
		 */
		@Name(MULTIPLE)
		boolean isMultiple();

		/**
		 * Setter for {@link #isMultiple()}.
		 */
		void setMultiple(boolean value);

	}

	/**
	 * Creates a {@link ResponseHeaderValue} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ResponseHeaderValue(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ResponseExtractor newExtractor(MethodSpec method) {
		return new ResponseExtractor() {
			
			@Override
			public Object extract(Call call, ClassicHttpResponse response) throws ProtocolException {
				String headerName = getConfig().getHeader();
				if (getConfig().isMultiple()) {
					return Arrays.stream(response.getHeaders(headerName))
						.map(Header::getValue)
						.collect(Collectors.toList());
				} else {
					Header header = response.getHeader(headerName);
					if (header == null) {
						return null;
					}
					return header.getValue();
				}
			}
		};
	}

}
