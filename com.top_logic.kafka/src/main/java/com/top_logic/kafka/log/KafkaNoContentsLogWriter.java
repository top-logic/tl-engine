/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.log;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.xml.TagWriter;

/**
 * {@link KafkaLogWriter} that does not log message contents.
 * 
 * <p>
 * Message contents is not written to the Kafka log.
 * </p>
 */
@InApp
@Label("No message contents")
public class KafkaNoContentsLogWriter implements KafkaLogWriter<Object> {

	@Override
	public void writeMetaData(TagWriter output, Object message) {
		output.beginBeginTag("ignored");
		output.endEmptyTag();
	}

	@Override
	public void writeAllData(TagWriter output, Object message) {
		output.beginBeginTag("ignored");
		output.endEmptyTag();
	}

}
