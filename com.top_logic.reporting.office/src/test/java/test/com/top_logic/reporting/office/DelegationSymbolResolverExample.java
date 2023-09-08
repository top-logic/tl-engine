/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.util.Properties;

import com.top_logic.reporting.office.AbstractDelegationSymbolResolver;
import com.top_logic.reporting.office.ExpansionContext;


/**
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class DelegationSymbolResolverExample extends
        AbstractDelegationSymbolResolver {

    /** 
     * We define only one method which simply returns if the expansion context is empty or not.
     * 
     * @see com.top_logic.reporting.office.AbstractDelegationSymbolResolver#initDelegationMap()
     */
    @Override
	protected void initDelegationMap() {
       delegationMap = new Properties();
       delegationMap.setProperty("EXAMPLE_KEY","examineContext");
    }
    
    public String examineContext (ExpansionContext aContext) {
        return aContext != null ? "we have something" : "nothing";
    }

}
