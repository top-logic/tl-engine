/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.json.JsonConverter;

/**
 * A {@link Deserializer} implementation which uses {@link JsonConverter} to
 * deserialize {@link Struct}s.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class StructDeserializer implements Deserializer<Struct> {

	/**
	 * The {@link JsonConverter} to be used for deserialization.
	 */
	private final JsonConverter _converter = new JsonConverter();
	
	@Override
	public void close() {
		// does nothing
	}

	@Override
	public void configure(final Map<String, ?> properties, final boolean isKey) {
		_converter.configure(properties, isKey);
	}

	@Override
	public Struct deserialize(final String topic, final byte[] data) {
		return (Struct) _converter.toConnectData(topic, data).value();
	}
}
