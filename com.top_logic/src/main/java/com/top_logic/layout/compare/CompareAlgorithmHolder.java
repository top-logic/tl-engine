/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Interface to implement by {@link LayoutComponent} when they are available to compare their model
 * with a model computed by the held {@link CompareAlgorithm}.
 *
 * @see CompareAlgorithm
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CompareAlgorithmHolder {

	/**
	 * Sets the {@link CompareAlgorithm} to compute the comparison model.
	 * 
	 * @param algorithm
	 *        The algorithm to use for computation of comparison model, or <code>null</code> when
	 *        comparison mode is switched off.
	 */
	void setCompareAlgorithm(CompareAlgorithm algorithm);

	/**
	 * Returns the held {@link CompareAlgorithm}.
	 * 
	 * @return The held {@link CompareAlgorithm}, or <code>null</code> to indicate that compare mode
	 *         is switched off.
	 */
	CompareAlgorithm getCompareAlgorithm();

}

