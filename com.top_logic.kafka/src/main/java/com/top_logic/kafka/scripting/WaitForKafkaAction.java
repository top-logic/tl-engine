/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.scripting;

import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.layout.scripting.action.SleepAction;

/**
 * Action to use to wait until kafka has sent and received the data.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WaitForKafkaAction extends SleepAction {

	@Override
	@LongDefault(1500)
	long getSleepTimeMillis();

}

