/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.tool;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.person.Homepage;
import com.top_logic.knowledge.wrap.person.HomepageImpl;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.view.component.FilteringFilterVO;

/**
 * {@link HomepageImpl} which can extract {@link FilterVO}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReportingHomepageImpl extends HomepageImpl {

	/**
	 * Creates a {@link ReportingHomepageImpl}.
	 */
	public ReportingHomepageImpl(InstantiationContext context, Homepage config) {
		super(context, config);
	}

	@Override
	protected ModelName extractTargetObjectFromModel(Object model) {
		if (model instanceof FilterVO) {
			model = ((FilterVO) model).getModel();
		} else if (model instanceof FilteringFilterVO) {
			model = ((FilteringFilterVO<?>) model).getModel();
		}
		return super.extractTargetObjectFromModel(model);
	}

}
