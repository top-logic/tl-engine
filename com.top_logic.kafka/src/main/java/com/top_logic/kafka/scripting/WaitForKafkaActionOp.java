/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.scripting;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.SleepActionOp;

/**
 * {@link AbstractApplicationActionOp} for {@link WaitForKafkaAction}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WaitForKafkaActionOp extends SleepActionOp<WaitForKafkaAction> {

	/**
	 * Creates a new {@link WaitForKafkaActionOp}.
	 */
	public WaitForKafkaActionOp(InstantiationContext context, WaitForKafkaAction config) {
		super(context, config);
	}

}

