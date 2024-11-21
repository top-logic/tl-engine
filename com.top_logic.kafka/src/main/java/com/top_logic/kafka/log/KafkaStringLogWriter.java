/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.log;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.xml.TagWriter;

/**
 * {@link KafkaLogWriter} for {@link String} messages.
 * 
 * <p>
 * Note: This implementation can only be used, if a string messages are sent. For a consumer, this
 * means that a string deserializer must be in use.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@InApp
@Label("Complete string message contents")
public class KafkaStringLogWriter implements KafkaLogWriter<String> {

	private static final String STRING = "string";

	private static final String SIZE = "size";

	private static final String HASH_CODE = "hash-code";

	private static final String CONTENT = "content";

	/** The {@link KafkaStringLogWriter} instance. */
	public static final KafkaStringLogWriter INSTANCE = new KafkaStringLogWriter();

	@Override
	public void writeMetaData(TagWriter output, String message) {
		output.beginTag(STRING);
		{
			KafkaLogUtil.writeTextTag(output, SIZE, message.length());
			KafkaLogUtil.writeTextTag(output, HASH_CODE, message.hashCode());
		}
		output.endTag(STRING);
	}

	@Override
	public void writeAllData(TagWriter output, String message) {
		output.beginTag(STRING);
		{
			KafkaLogUtil.writeTextTag(output, SIZE, message.length());
			KafkaLogUtil.writeTextTag(output, HASH_CODE, message.hashCode());
			KafkaLogUtil.writeTextTag(output, CONTENT, message);
		}
		output.endTag(STRING);
	}

}
