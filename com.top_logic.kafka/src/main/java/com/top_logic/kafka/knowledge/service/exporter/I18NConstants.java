/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.exporter;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 LOCK_TIMED_OUT__TIME;

	public static ResKey SENDING_IN_PROGRESS;

	public static ResKey NO_CHANGES;

	public static ResKey ERROR_SEND_TO_KAFKA_TIMEOUT;

	public static ResKey ERROR_SEND_TO_KAFKA_EXCEPTION;

	public static ResKey ERROR_SEND_TO_KAFKA_INTERRUPTED;

	public static ResKey2 SEND_TO_KAFKA_SUCCESSFUL__START_REVISION__STOP_REVISION;

	public static ResKey CANCELED_DURING_STARTUP;

	public static ResKey2 CANCELED_DURING_PROCESSING__START_REVISION__LAST_PROCESSED;

	public static ResKey SEND_FAILED_UNHANDLED_EXCEPTION_BUT_PROGRESS;

	public static ResKey SEND_FAILED_UNHANDLED_EXCEPTION_NO_PROGRESS;

	public static ResKey1 SKIPPING_DUE_TO_EARLIER_PROBLEMS__RESUME_TIME;

	public static ResKey2 WRITE_FAILED_BUT_PROGRESS__REVISION__EXCEPTION;

	public static ResKey2 WRITE_FAILED_NO_PROGRESS__REVISION__EXCEPTION;

	static {
		initConstants(I18NConstants.class);
	}
}
