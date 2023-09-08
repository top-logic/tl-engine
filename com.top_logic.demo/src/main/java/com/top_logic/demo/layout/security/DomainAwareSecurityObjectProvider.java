/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.security;

import com.top_logic.element.layout.admin.StructureDomainMapper;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DomainAwareSecurityObjectProvider implements SecurityObjectProvider {

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		LayoutComponent component = ((LayoutComponent) aChecker);
		CompoundSecurityLayout compoundLayout = CompoundSecurityLayout.getNearestCompoundLayout(component);
		String securityDomain = (String) new StructureDomainMapper().map(compoundLayout);
//		String securityDomain = compoundLayout.getSecurityDomain();
		StructuredElementFactory factory = (StructuredElementFactory) DynamicModelService.getFactoryFor(securityDomain);
		return (BoundObject) factory.getRoot();
	}

}
