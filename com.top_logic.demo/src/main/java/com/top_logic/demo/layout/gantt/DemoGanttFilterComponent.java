/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.gantt;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.demo.model.types.DemoTypesAll;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.reporting.chart.gantt.component.GanttChartFilterComponent;
import com.top_logic.reporting.chart.gantt.component.form.AbstractContextDatesDependency;

/**
 * Demo {@link GanttChartFilterComponent}.
 * 
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class DemoGanttFilterComponent extends GanttChartFilterComponent {

	/**
	 * Creates a {@link DemoGanttFilterComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DemoGanttFilterComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected AbstractContextDatesDependency createContextDatesDependency(FormContext context) {
		return null;
	}


	@Override
	protected List<? extends Wrapper> getCommitteeList() {
		return Collections.emptyList();
	}


	@Override
	protected boolean supportsInternalModel(Object aObject) {
		return aObject instanceof DemoTypesAll;
	}

}
