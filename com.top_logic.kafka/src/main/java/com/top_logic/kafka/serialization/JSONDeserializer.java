/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;

/**
 * A {@link Deserializer} interprets contents as JSON encoded objects.
 */
@InApp
public class JSONDeserializer extends AbstractConfiguredInstance<JSONDeserializer.Config<?>>
		implements Deserializer<Object> {

	/**
	 * Configuration options for {@link JSONDeserializer}.
	 */
	public interface Config<I extends JSONDeserializer> extends PolymorphicConfiguration<I> {
		/**
		 * The encoding of message contents.
		 */
		@StringDefault(StringServices.UTF8)
		String getEncoding();
	}

	private final Charset _encoding;

	/**
	 * Creates a {@link JSONDeserializer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public JSONDeserializer(InstantiationContext context, Config<?> config) {
		super(context, config);
		_encoding = Charset.forName(config.getEncoding());
	}

	@Override
	public Object deserialize(String topic, byte[] data) {
		try {
			return JSON.read(new InputStreamReader(new ByteArrayInputStream(data), _encoding));
		} catch (ParseException ex) {
			throw new SerializationException(ex);
		}
	}

}
