/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.List;

/**
 * {@link AdditionalValuesTemplateProvider} returning a constant template path.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConstantAdditionalValuesTemplateProvider implements AdditionalValuesTemplateProvider {

	private final String _template;

	/**
	 * Creates a new {@link ConstantAdditionalValuesTemplateProvider}.
	 */
	public ConstantAdditionalValuesTemplateProvider(String template) {
		_template = template;
	}

	@Override
	public String getTemplate(List<String> columns) {
		return _template;
	}

}

