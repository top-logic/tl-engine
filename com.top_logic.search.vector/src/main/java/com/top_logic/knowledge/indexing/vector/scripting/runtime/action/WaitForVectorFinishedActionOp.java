/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.vector.scripting.runtime.action;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.knowledge.indexing.vector.VectorIndex;
import com.top_logic.knowledge.indexing.vector.scripting.action.WaitForVectorFinishedAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Default implementation for usage of {@link WaitForVectorFinishedAction}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WaitForVectorFinishedActionOp extends AbstractApplicationActionOp<WaitForVectorFinishedAction> {

	/**
	 * Creates a new {@link WaitForVectorFinishedActionOp} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link WaitForVectorFinishedActionOp}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public WaitForVectorFinishedActionOp(InstantiationContext context, WaitForVectorFinishedAction config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		long maxSleep = config.getMaxSleep();
		if (maxSleep <=0) {
			VectorIndex vectorIndex = VectorIndex.getInstance();
			int numberObjectsForIndex = vectorIndex.queueSizes();
			if (numberObjectsForIndex > 0) {
				throw ApplicationAssertions.fail(config, "VectorIndex did not finished work. Still "
					+ numberObjectsForIndex + " objects for indexing. Do not wait.");
			}
		}

		int numberObjectsForIndex = waitForVector(maxSleep);

		if (numberObjectsForIndex > 0) {
			ApplicationAssertions.fail(config, "VectorIndex did not finished work in time. Still "
				+ numberObjectsForIndex + " objects for indexing. Waited " + StopWatch.toStringMillis(maxSleep) + ".");
		}

		return argument;
	}

	private int waitForVector(long remainingTime) throws InterruptedException {
		long sleepTime = 10;
		while (true) {
			VectorIndex vectorIndex = VectorIndex.getInstance();
			if (vectorIndex.isIdle()) {
				// Vector does its work. Stop here.
				return 0;
			}
			if (remainingTime < 0) {
				return vectorIndex.queueSizes();
			}
			remainingTime -= sleepTime;
			Thread.sleep(sleepTime);
		}
	}

}
