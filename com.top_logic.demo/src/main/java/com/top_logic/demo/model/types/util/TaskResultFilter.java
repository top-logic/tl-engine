/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mail.proxy.I18NConstants;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TaskResultFilter implements Filter<TaskResult> {

	@Override
	public boolean accept(TaskResult anObject) {
		ResKey message = anObject.getMessage();
		if (!I18NConstants.FETCH_MAILS_COMPLETED_SUCCESSFULLY__COUNT_DURATION.equals(message.plain())) {
			throw handleIllegalMessagePattern(anObject);
		}

		return ((Integer) message.arguments()[0]) > 0;
	}

	private static IllegalStateException handleIllegalMessagePattern(TaskResult result) {
		StringBuilder unexpectedFormat = new StringBuilder();
		unexpectedFormat.append("Format of message of TaskResult '");
		unexpectedFormat.append(result);
		unexpectedFormat.append("' is unexpected: expected a message with key '");
		unexpectedFormat.append(I18NConstants.FETCH_MAILS_COMPLETED_SUCCESSFULLY__COUNT_DURATION);
		unexpectedFormat.append("'.");
		throw new IllegalStateException(unexpectedFormat.toString());
	}

}

