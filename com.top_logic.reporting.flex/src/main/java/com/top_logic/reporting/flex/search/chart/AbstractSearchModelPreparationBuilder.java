/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.chart;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.reporting.flex.chart.config.model.AbstractModelPreparationBuilder;

/**
 * {@link AbstractModelPreparationBuilder} that works together with
 * {@link SearchResultInteractiveChartBuilder} providing separate UIs for partitions and
 * aggregations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSearchModelPreparationBuilder extends AbstractModelPreparationBuilder {

	/**
	 * Creates a {@link AbstractSearchModelPreparationBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractSearchModelPreparationBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

}
