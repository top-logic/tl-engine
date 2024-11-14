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
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.xml.TagWriter;

/**
 * A generic {@link Serializer} that is compatible with {@link BinaryData}, {@link List},
 * {@link Map}, {@link HTMLFragment} and {@link String}.
 * 
 * <p>
 * Contents of {@link BinaryData} is transmitted as is. {@link List} and {@link Map} values are
 * transmitted JSON-encoded. {@link HTMLFragment} are transmitted as XML with the given encoding.
 * All other values are converted to {@link String} and transmitted with the given encoding.
 * </p>
 */
@InApp
public class GenericSerializer extends AbstractConfiguredInstance<GenericSerializer.Config<?>>
		implements Serializer<Object> {

	/**
	 * Configuration options for {@link GenericSerializer}.
	 */
	public interface Config<I extends GenericSerializer> extends PolymorphicConfiguration<I> {

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
	 * Creates a {@link GenericSerializer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GenericSerializer(InstantiationContext context, Config<?> config) {
		super(context, config);
		_encoding = Charset.forName(config.getEncoding());
	}

	@Override
	public byte[] serialize(String topic, Object data) {
		return doSerialize(data, _encoding);
	}

	/**
	 * Converts values to byte arrays, either directly (binary data), through JSON encoding (lists
	 * and maps), or by converting to string.
	 */
	public static byte[] doSerialize(Object data, Charset encoding) {
		try {
			return trySerialize(data, encoding);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	private static byte[] trySerialize(Object data, Charset encoding) throws IOException {
		if (data instanceof BinaryDataSource binary) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			binary.deliverTo(buffer);
			return buffer.toByteArray();
		}
		
		if (data instanceof List || data instanceof Map) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			try (OutputStreamWriter out = new OutputStreamWriter(buffer, encoding)) {
				JSON.write(out, data);
			}
			return buffer.toByteArray();
		}
		
		if (data instanceof HTMLFragment xml) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			try (OutputStreamWriter out = new OutputStreamWriter(buffer, encoding)) {
				try (TagWriter tagOut = new TagWriter(out)) {
					xml.write(null, tagOut);
				}
			}
			return buffer.toByteArray();
		}

		if (data == null) {
			return new byte[0];
		}
		
		String string = data.toString();
		return string.getBytes(encoding);
	}

}
