/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.element.structured.StructuredElement;

/**
 * Locator for resolving some structured element depending references.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StructuredElementLocator extends CustomSingleSourceValueLocator {

	public static final String PARENT_LOCATOR = StructuredElement.PARENT_ATTR;

    public static final String CONTEXT_LOCATOR = "context";

    public static final String ROOT_LOCATOR = "root";

	public static final String CHILDREN_LOCATOR = StructuredElement.CHILDREN_ATTR;

    private final boolean isParent;

    private final boolean isContext;
    
    private final boolean isRoot;
    
    private final boolean isChildren;

    /**
     * Create a new PathAttributeValueLocator with attribute path config
     * 
     * @param    aConfig    The config
     */
    public StructuredElementLocator(String aConfig) {
        super();

        this.isContext  = CONTEXT_LOCATOR.equals(aConfig);
        this.isRoot     = ROOT_LOCATOR.equals(aConfig);
        this.isChildren = CHILDREN_LOCATOR.equals(aConfig);
        this.isParent   = !this.isContext && !this.isRoot && !this.isChildren;
    }

    @Override
	public Object internalLocateAttributeValue(Object anObject) {
        if (anObject instanceof StructuredElement) {
            StructuredElement theElement = (StructuredElement) anObject;

            return this.isContext ? theElement.getStructureContext() 
                                  : this.isRoot ? theElement.getRoot() 
                                                : this.isChildren ? theElement.getChildren() 
                                                                  : theElement.getParent();
        }
        else {
            return null;
        }
    }
}
