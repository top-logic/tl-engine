/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart.flex;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} for {@link ChartRef}s showing only the file name without path.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChartRefLabels implements LabelProvider {

	/**
	 * Singleton {@link ChartRefLabels} instance.
	 */
	public static final ChartRefLabels INSTANCE = new ChartRefLabels();

	private ChartRefLabels() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		NamedConfiguration option = (NamedConfiguration) object;
		String name = option.getName();
		int slashIndex = name.lastIndexOf('/');
		if (slashIndex >= 0) {
			return name.substring(slashIndex + 1);
		}
		return name;
	}
}