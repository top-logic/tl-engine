/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.json.JSON;

/**
 * A {@link Serializer} encodes values as JSON objects.
 * 
 * <p>
 * In contrast to the {@link GenericSerializer}, also primitive values such as numbers and strings
 * are JSON encoded.
 * </p>
 */
@InApp
public class JSONSerializer extends AbstractConfiguredInstance<JSONSerializer.Config<?>>
		implements Serializer<Object> {

	/**
	 * Configuration options for {@link JSONSerializer}.
	 */
	public interface Config<I extends JSONSerializer> extends PolymorphicConfiguration<I> {

		/**
		 * The encoding for character contents.
		 */
		@StringDefault(StringServices.UTF8)
		String getEncoding();
	}

	private final Charset _encoding;

	/**
	 * Creates a {@link JSONSerializer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public JSONSerializer(InstantiationContext context, Config<?> config) {
		super(context, config);
		_encoding = Charset.forName(config.getEncoding());
	}

	@Override
	public byte[] serialize(String topic, Object data) {
		try {
			return trySerialize(data);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	private byte[] trySerialize(Object data) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		JSON.write(new OutputStreamWriter(buffer, _encoding), data);
		return buffer.toByteArray();
	}

}
