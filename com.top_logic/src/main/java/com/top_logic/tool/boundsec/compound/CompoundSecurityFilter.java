/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.gui.profile.ComponentAware;
import com.top_logic.util.Utils;

/**
 * {@link Filter} for {@link CompoundSecurityLayout}s
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class CompoundSecurityFilter implements Filter,ComponentAware {

    LayoutComponent component;
    
    @Override
	public boolean accept(Object anObject) {
        if (anObject instanceof CompoundSecurityLayout) {
            Selectable theSelectableMaster = this.component.getMaster().getSelectableMaster();
            if (theSelectableMaster == null) {
                // if there is no filter, we allow all components
                return true;
            }
            Mapping theSecurityDomainMapper = CompoundSecurityLayout.getSecurityDomainMapper();
            Object theFilterDomain = theSecurityDomainMapper.map(theSelectableMaster.getSelected());
            Object theDomain       = theSecurityDomainMapper.map(anObject);
            // if no domain is specified, we provide the component in all domains
            return theDomain == null || Utils.equals(theFilterDomain, theDomain);
        } else {
            return false;
        }
    }
    
    @Override
	public void setComponent(LayoutComponent aComponent) {
        this.component = aComponent;
    }
}

