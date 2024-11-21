/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import java.nio.charset.Charset;

import org.apache.kafka.common.serialization.Deserializer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * A {@link Deserializer} interprets contents as text in a given encoding.
 */
@InApp
public class StringDeserializer extends AbstractConfiguredInstance<StringDeserializer.Config<?>>
		implements Deserializer<String> {

	/**
	 * Configuration options for {@link StringDeserializer}.
	 */
	public interface Config<I extends StringDeserializer> extends PolymorphicConfiguration<I> {
		/**
		 * The encoding of message contents.
		 */
		@StringDefault(StringServices.UTF8)
		String getEncoding();
	}

	private final Charset _encoding;

	/**
	 * Creates a {@link StringDeserializer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StringDeserializer(InstantiationContext context, Config<?> config) {
		super(context, config);
		_encoding = Charset.forName(config.getEncoding());
	}

	@Override
	public String deserialize(String topic, byte[] data) {
		return new String(data, _encoding);
	}

}
