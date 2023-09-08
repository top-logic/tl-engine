/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * Extend the superclass to allow a Person to edit itself.
 * 
 * @deprecated use EditPersonComponent when using new security system with role rules.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
@Deprecated
public class SelfEditPersonComponent  extends EditPersonComponent {

    /**
     * Constructor using {@link org.xml.sax.Attributes} from XML representation.
     * 
     * @param    someAttrs       Attributes to configure this component.
     */
    public SelfEditPersonComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException{
        super(context, someAttrs);
    }
    

    /**
     * Allow any Person to edit itself, call super with the current Person.
     * 
     * In new Project this is specified using Rules
     * 
     * @see com.top_logic.tool.boundsec.BoundComponent#allow(com.top_logic.tool.boundsec.BoundCommandGroup, com.top_logic.tool.boundsec.BoundObject)
     */
    @Override
	public boolean allow(BoundCommandGroup aCmdGroup, BoundObject anObject) {
        TLContext tContext  = TLContext.getContext();
        boolean   theResult = false; 
        if (tContext != null) {
            theResult = super.allow(aCmdGroup, tContext.getCurrentPersonWrapper());
            if (!theResult) {
                // Allow any Person to edit itself by default
				{
                    boolean isCurrentPerson = Utils.equals(PersonManager.getManager().getCurrentPerson(), anObject);
                    boolean isReadWrite = SimpleBoundCommandGroup.READWRITE_SET.contains(aCmdGroup);
                    theResult = isCurrentPerson && isReadWrite;
                }
            }
        }
        return theResult;
    }
    
}
