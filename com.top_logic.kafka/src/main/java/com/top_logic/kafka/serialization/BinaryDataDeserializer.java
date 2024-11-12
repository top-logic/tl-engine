/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import org.apache.kafka.common.serialization.Deserializer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * A {@link Deserializer} interprets contents as binary data that must be further processed by the
 * message processor.
 */
@InApp
public class BinaryDataDeserializer extends AbstractConfiguredInstance<BinaryDataDeserializer.Config<?>>
		implements Deserializer<BinaryData> {

	/**
	 * Configuration options for {@link BinaryDataDeserializer}.
	 */
	public interface Config<I extends BinaryDataDeserializer> extends PolymorphicConfiguration<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link BinaryDataDeserializer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BinaryDataDeserializer(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public BinaryData deserialize(String topic, byte[] data) {
		return BinaryDataFactory.createBinaryData(data);
	}

}
