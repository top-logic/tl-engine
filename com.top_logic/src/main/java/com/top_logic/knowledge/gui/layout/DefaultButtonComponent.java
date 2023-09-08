/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.util.Collection;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * Use instead of ButtonComponent to avoid extra configuration.
 * 
 * <ul>
 *  <li>Will automatically connect itself to next Sibling in Parent</li>
 *  <li>No need to specify a name, will be derived as target.getName() + "Buttons"</li>
 *  <li>Will not work in special cases</li>
 * </ul>
 * 
 * TODO KHA/BHU move this as default into ButtonComponent?
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DefaultButtonComponent extends ButtonComponent {

    /** 
     * Creates a new DefaultButtonComponent from XML.
     */
    public DefaultButtonComponent(InstantiationContext context, Config aAnAttributes) throws ConfigurationException {
        super(context, aAnAttributes);
    }
    
    /**
     * Connect myself to previous sibling in Parent.
     */
    @Override
	public void setParent(LayoutComponent aParent) {
        super.setParent(aParent);

		if (aParent instanceof LayoutContainer) {
			Collection<LayoutComponent> siblings = ((LayoutContainer) aParent).getChildList();
            LayoutComponent target = null;
			for (LayoutComponent sibling : siblings) {
                if (sibling == this) {
                    break;
                }
                target = sibling;
            }
            if (target != null) {
                target.setButtonComponent(this);
            }
        }
    }

}

