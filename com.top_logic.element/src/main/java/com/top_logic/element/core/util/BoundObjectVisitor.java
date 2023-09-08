/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.util;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * Collect all elements, where the defined bound checker allows the defined command group.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class BoundObjectVisitor extends AllElementVisitor {

    /** The checker to be used for testing the access. */
    private BoundChecker checker;

    /** The command group to be tested. */
    private BoundCommandGroup commandGroup;

    /** 
     * Create a new BoundObjectVisitor with estimatedSize.
     */
    public BoundObjectVisitor(int estimatedSize, BoundChecker aChecker, BoundCommandGroup aGroup) {
        super(estimatedSize);
        this.checker      = aChecker;
        this.commandGroup = aGroup;
    }

    /** 
     * Create a new  new BoundObjectVisitor
     */
    public BoundObjectVisitor(BoundChecker aChecker, BoundCommandGroup aGroup) {
        this.checker      = aChecker;
        this.commandGroup = aGroup;
    }

    /** 
     * Perform the security check.
     * 
     * If the give element is no bound object, the method will return <code>false</code>, 
     * which will result in an stop of traversing in the children of the given element.
     * 
     * @param    anElement    The element to be checked.
     * @param    aDepth       The current depth in the element hierachie.
     * @return   <code>true</code>, if given element is a bound object, <code>false</code> otherwise.
     * @see      com.top_logic.element.core.TLElementVisitor#onVisit(com.top_logic.element.structured.StructuredElement, int)
     */
    @Override
	public boolean onVisit(StructuredElement anElement, int aDepth) {
        if (anElement instanceof BoundObject) {
            BoundObject theObject = (BoundObject) anElement;

            if (this.checker.allow(this.commandGroup, theObject)) {
                super.onVisit(anElement, aDepth);
            }

            return (true);
        }

        return (false);
    }
}
