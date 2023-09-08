/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.name;

import com.top_logic.layout.AbstractResourceProvider;

/**
 * A {@link NamableResourceProvider} shows the name of a {@link Namable}.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class NamableResourceProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		if (!(object instanceof Namable)) return null; 
		
		return ((Namable) object).getName();
	}
	
	@Override
	public String getType(Object anObject) {
		return Namable.class.getName();
	}

}
