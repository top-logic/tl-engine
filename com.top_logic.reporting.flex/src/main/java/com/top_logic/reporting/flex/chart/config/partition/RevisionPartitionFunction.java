/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.knowledge.service.Revision;

/**
 * Simple {@link PartitionFunction} to provide a List of {@link Revision}s for futher partitioning.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class RevisionPartitionFunction<C extends RevisionPartitionFunction.Config> extends AbstractRevisionPartitionFunction<C> {

	/**
	 * Config-interface for {@link RevisionPartitionFunction}.
	 */
	public interface Config extends AbstractRevisionPartitionFunction.Config {

		/**
		 * the list of revisions used as partition-keys
		 */
		@ListBinding(format = RevisionValueProvider.class, attribute = "rev", tag = "revision")
		List<Revision> getRevisions();

	}


	/**
	 * Config-constructor for {@link RevisionPartitionFunction}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public RevisionPartitionFunction(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected List<Revision> getRevisions() {
		return getConfig().getRevisions();
	}

}