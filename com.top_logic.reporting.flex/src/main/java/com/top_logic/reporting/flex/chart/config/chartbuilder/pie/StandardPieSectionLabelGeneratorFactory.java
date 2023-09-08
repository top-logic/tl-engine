/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.pie;

import java.util.Locale;

import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;

/**
 * {@link PieSectionLabelGeneratorFactory} creating {@link StandardPieSectionLabelGeneratorFactory}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StandardPieSectionLabelGeneratorFactory implements PieSectionLabelGeneratorFactory {

	/** Singleton {@link StandardPieSectionLabelGeneratorFactory} instance. */
	public static final StandardPieSectionLabelGeneratorFactory INSTANCE =
		new StandardPieSectionLabelGeneratorFactory();

	private StandardPieSectionLabelGeneratorFactory() {
		// singleton instance
	}

	@Override
	public PieSectionLabelGenerator newGenerator(Locale l) {
		return new StandardPieSectionLabelGenerator("{0}: {1}", l);
	}

}

