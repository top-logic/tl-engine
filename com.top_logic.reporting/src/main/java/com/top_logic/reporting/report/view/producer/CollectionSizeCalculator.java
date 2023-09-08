/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.util.Collection;

import com.top_logic.util.Resources;

/**
 * Returns the size of the given collection.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CollectionSizeCalculator extends AbstractCollectionToNumberCalculator {

	@Override
	public double calculateNumber(Collection<Object> someRiskItems) {
	    return (someRiskItems == null) ? 0 : someRiskItems.size();
	}

	@Override
	public String getXAxisLabel() {
		return Resources.getInstance().getMessage(I18NConstants.OVERVIEW_SUM, Integer.valueOf((int) getTotalValue()));
	}

	@Override
	public String getYAxisLabel() {
		return Resources.getInstance().getString(I18NConstants.QUANTITY_AXIS_LABEL);
	}

	@Override
	public String getName() {
		return Resources.getInstance().getString(I18NConstants.COLLECTION_SIZE_CALCULATOR);
	}

}
