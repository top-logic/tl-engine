/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.tool;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.view.component.FilteringFilterVO;
import com.top_logic.tool.boundsec.commandhandlers.CompoundSetHomepageHandler;

/**
 * {@link CompoundSetHomepageHandler} which can extract {@link FilterVO}s.
 * 
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ReportingSetHomepageHandler extends CompoundSetHomepageHandler {

	/**
	 * Creates a new {@link ReportingSetHomepageHandler}.
	 */
	public ReportingSetHomepageHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ModelName extractTargetObjectFromModel(Object model) {
		if (model instanceof FilterVO) {
			model = ((FilterVO) model).getModel();
		}
		else if (model instanceof FilteringFilterVO) {
			model = ((FilteringFilterVO<?>) model).getModel();
		}
		return super.extractTargetObjectFromModel(model);
	}

}
