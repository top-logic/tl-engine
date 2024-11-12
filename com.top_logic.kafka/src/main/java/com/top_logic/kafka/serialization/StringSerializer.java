/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import java.nio.charset.Charset;

import org.apache.kafka.common.serialization.Serializer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * A {@link Serializer} that creates messages by interpreting values as {@link String}s and encodes
 * them with a given character set.
 */
@InApp
public class StringSerializer extends AbstractConfiguredInstance<StringSerializer.Config<?>>
		implements Serializer<Object> {

	/**
	 * Configuration options for {@link StringSerializer}.
	 */
	public interface Config<I extends StringSerializer> extends PolymorphicConfiguration<I> {

		/**
		 * The encoding for character contents.
		 * 
		 * <p>
		 * The value is irrelevant, if binary data is transmitted.
		 * </p>
		 */
		@StringDefault(StringServices.UTF8)
		String getEncoding();
	}

	private final Charset _encoding;

	/**
	 * Creates a {@link StringSerializer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StringSerializer(InstantiationContext context, Config<?> config) {
		super(context, config);
		_encoding = Charset.forName(config.getEncoding());
	}

	@Override
	public byte[] serialize(String topic, Object data) {
		String string = data == null ? "" : data.toString();
		return string.getBytes(_encoding);
	}

}
