/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;

/**
 * Implementing classes are responsible for processing {@link ConsumerRecords}
 * receive by {@link ConsumerDispatcher}s.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public interface ConsumerProcessor<K, V> {
	
	/**
	 * Process the given records.
	 * 
	 * @param records
	 *            the {@link ConsumerRecords} to be processed
	 */
	void process(ConsumerRecords<K, V> records);
}