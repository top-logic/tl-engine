/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

/**
 * Common super class for {@link Serializer} in TL which are not configured via Kafka.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TLSerializer<T> implements Serializer<T> {

	@Override
	public final void configure(Map<String, ?> configs, boolean isKey) {
		// Nothing to configure here
	}

}

