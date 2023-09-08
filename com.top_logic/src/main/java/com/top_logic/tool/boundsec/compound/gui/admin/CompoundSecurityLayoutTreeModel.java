/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.gui.BoundCheckerRoleProfileTreeModel;

/**
 * POS security specific layout tree. Only project layouts are part of this tree
 * because in the pos secirity the role profiles are defined via project layouts
 * only.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class CompoundSecurityLayoutTreeModel extends BoundCheckerRoleProfileTreeModel {

    String  securityDomain;
    Mapping domainMapper;
    
    
    /**
     * Constructor
     */
    public CompoundSecurityLayoutTreeModel(BoundChecker aRoot, String aSecurityDomain, Mapping aDomainMapper) {
        super(aRoot);
        this.securityDomain = aSecurityDomain;
        this.domainMapper   = aDomainMapper;
    }
    
    /**
     * Constructor
     */
    public CompoundSecurityLayoutTreeModel(BoundChecker aRoot) {
        super(aRoot);
        this.domainMapper = new Mapping() {
            @Override
			public Object map(Object aInput) {
                return null;
            }
        };
    }

    /**
     * Children of a given node are all project layouts in the sub-tree 
     * under the given node for which the path do not contain another project layout.
     * 
     * @param aChecker          the node to get the children for
     * @param aChildList        the list to add the children to
     * @param aRepresentedList  contains all  real children (ask KBU for explanation)
     *
     * @see com.top_logic.tool.boundsec.gui.BoundCheckerRoleProfileTreeModel#getChildren(com.top_logic.tool.boundsec.BoundChecker, java.util.List, java.util.List)
     */
	@Override
	protected void getChildren(BoundChecker aChecker, List aChildList,
            List aRepresentedList) {
        
	    if (aChecker == null) {
            return;
        }
        
		Collection<? extends BoundChecker> theBoundChildren = aChecker.getChildCheckers();
        if (theBoundChildren == null) {
            return;
        }

		Iterator<? extends BoundChecker> theBoundChildrenIt = theBoundChildren.iterator();
        while (theBoundChildrenIt.hasNext()) {
			BoundChecker theBoundChild = theBoundChildrenIt.next();

            aRepresentedList.add(theBoundChild);

            if (theBoundChild instanceof CompoundSecurityLayout) { // show this
                CompoundSecurityLayout thePotentialChild = (CompoundSecurityLayout) theBoundChild;
                String                 theSecurityDomain  = (String) this.domainMapper.map(thePotentialChild);
                if (theSecurityDomain == null) {
                            theSecurityDomain = thePotentialChild.getSecurityDomain();
                }
                if (StringServices.isEmpty(this.securityDomain) || StringServices.isEmpty(theSecurityDomain) || theSecurityDomain.equals(this.securityDomain)) {
                    aChildList.add(theBoundChild);
                } else if (getChildCount(theBoundChild) > 0) {
                    aChildList.add(theBoundChild);
                }
			} else {
                this.getChildren(theBoundChild, aChildList, aRepresentedList);
            }
        }

    }
}
