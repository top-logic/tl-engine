/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.log;

import com.top_logic.basic.xml.TagWriter;

/**
 * The {@link KafkaLogWriter} for {@link String} messages.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
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
