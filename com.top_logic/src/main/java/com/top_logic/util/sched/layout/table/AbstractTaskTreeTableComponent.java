/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.util.sched.task.Task;

/**
 * @author     <a href="mailto:olb@top-logic.com">olb</a>
 */
public abstract class AbstractTaskTreeTableComponent extends TreeTableComponent {

	/**
	 * Creates a {@link AbstractTaskTreeTableComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractTaskTreeTableComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object == null || object instanceof Task;
	}
}
