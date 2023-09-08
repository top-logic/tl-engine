/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.lucene.scripting.runtime.action;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.indexing.lucene.scripting.action.WaitForLuceneFinishedAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Default implementation for usage of {@link WaitForLuceneFinishedAction}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WaitForLuceneFinishedActionOp extends AbstractApplicationActionOp<WaitForLuceneFinishedAction> {

	/**
	 * Creates a new {@link WaitForLuceneFinishedActionOp} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link WaitForLuceneFinishedActionOp}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public WaitForLuceneFinishedActionOp(InstantiationContext context, WaitForLuceneFinishedAction config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		long maxSleep = config.getMaxSleep();
		if (maxSleep <=0) {
			LuceneIndex luceneIndex = LuceneIndex.getInstance();
			int numberObjectsForIndex = luceneIndex.queueSizes();
			if (numberObjectsForIndex > 0) {
				throw ApplicationAssertions.fail(config, "LuceneIndex did not finished work. Still "
					+ numberObjectsForIndex + " objects for indexing. Do not wait.");
			}
		}

		int numberObjectsForIndex = waitForLucene(maxSleep);

		if (numberObjectsForIndex > 0) {
			ApplicationAssertions.fail(config, "LuceneIndex did not finished work in time. Still "
				+ numberObjectsForIndex + " objects for indexing. Waited " + StopWatch.toStringMillis(maxSleep) + ".");
		}

		return argument;
	}

	private int waitForLucene(long remainingTime) throws InterruptedException {
		long sleepTime = 10;
		while (true) {
			LuceneIndex luceneIndex = LuceneIndex.getInstance();
			if (luceneIndex.isIdle()) {
				// Lucene does its work. Stop here.
				return 0;
			}
			if (remainingTime < 0) {
				return luceneIndex.queueSizes();
			}
			remainingTime -= sleepTime;
			Thread.sleep(sleepTime);
		}
	}

}
