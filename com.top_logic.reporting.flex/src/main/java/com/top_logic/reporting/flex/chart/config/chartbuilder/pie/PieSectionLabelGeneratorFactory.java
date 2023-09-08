/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.pie;

import java.util.Locale;

import org.jfree.chart.labels.PieSectionLabelGenerator;

/**
 * Factory for {@link PieSectionLabelGenerator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PieSectionLabelGeneratorFactory {

	/**
	 * Creates a {@link PieSectionLabelGenerator} for the given locale.
	 * 
	 * @param l
	 *        The {@link Locale} to create label for.
	 * @return May be <code>null</code>.
	 */
	PieSectionLabelGenerator newGenerator(Locale l);
}

