/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import com.top_logic.demo.model.plain.DemoPlainA;
import com.top_logic.demo.model.types.DemoTypesL;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;

/**
 * The {@link ResourceProvider} for {@link DemoYearGroup}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoYearGroupResourceProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		DemoYearGroup demoYearGroup = (DemoYearGroup) object;
		if (demoYearGroup.getYear() == null) {
			return "No year";
		}
		return "Year " + demoYearGroup.getYear();
	}

	@Override
	public String getTooltip(Object anObject) {
		return "Synthetic node grouping the " + DemoPlainA.class.getSimpleName() + " children of a "
			+ DemoTypesL.class.getSimpleName() + " by the year of their date.";
	}

}
