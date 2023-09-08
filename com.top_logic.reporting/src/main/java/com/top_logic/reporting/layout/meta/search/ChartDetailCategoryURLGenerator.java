/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import org.jfree.data.category.CategoryDataset;

import com.top_logic.base.chart.generator.url.AjaxArgumentsCategoryURLGenerator;

/**
 * {@link AjaxArgumentsCategoryURLGenerator} with an unclear additional constant first argument to
 * the invoked function.
 * 
 * <p>
 * TODO: Remove or explain.
 * </p>
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class ChartDetailCategoryURLGenerator extends AjaxArgumentsCategoryURLGenerator {

	private final String name;

	public ChartDetailCategoryURLGenerator(String name) {
		this.name = name;
	}

	@Override
	public String generateURL(CategoryDataset data, int aSeries, int aCategory) {
		StringBuffer theOnClickFragment = new StringBuffer();
		
		theOnClickFragment.append("javascript:");
		theOnClickFragment.append(this.name + "(");
		theOnClickFragment.append("\'" + "chart" + "\',");
		theOnClickFragment.append("\'" + aSeries + "\',");
		theOnClickFragment.append("\'" + aCategory + "\')");
		
		return theOnClickFragment.toString();
	}

}
