/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import java.util.Set;
import java.util.TreeSet;

import com.top_logic.element.core.TLElementVisitor;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.wrap.WrapperNameComparator;

/**
 * Collects all {@link StructuredElement}s below the one defined in the constructor.
 *
 * The Visitor will stop on the first duplicate element found, thus it can operate on 
 * cyclic structures, too. It will not Collect Elements where the Parent was not collected
 * before (may happen with multiple Parents.) In case of duplicates different traversal
 * strategies will collect different results. {@link TraversalFactory#ANCESTORS} is not supported.
 * 
 * In case you do not need the features above better use the {@link AllElementVisitor} which will be much faster.
 *
 * @author   <a href=mailto:mga@top-logic.com>mga</a>
 * @author   Dirk Köhlhoff
 */
public class CollectAllBelowVisitor implements TLElementVisitor {

    /** Stores all visited elements, {@link TreeSet} to exclude duplicates */
    private final Set<StructuredElement> collected; 

    /** Start element to be kept away from result set. */
    private StructuredElement startSE;

    /**
     * Default constructor, which needs the element to start collecting.
     * 
     * @param    anElement                   That is to be suppressed, must not be <code>null</code>.
     * @param    toCollect                   The Set used to collect the Elements, must not be <code>null</code>.
     * @throws   NullPointerException        If one of the arguments is <code>null</code>.
     */
    public CollectAllBelowVisitor(StructuredElement anElement, Set<StructuredElement> toCollect) throws IllegalArgumentException {
        if (anElement == null) {
            throw new NullPointerException("CollectAllBelowVisitor without a start element"); 
        }
        if (toCollect == null) {
            throw new NullPointerException("toCollect"); 
        }

        this.collected = toCollect;
        this.startSE   = anElement;
    }

    /**
     * Constructor using a TreeSet with a {@link WrapperNameComparator}.
     * 
     * @param    anElement                   That is to be suppressed, must not be <code>null</code>.
     * @throws   NullPointerException        If anElement is <code>null</code>.
     */
    public CollectAllBelowVisitor(StructuredElement anElement) throws IllegalArgumentException {
		this(anElement, new TreeSet<>(new WrapperNameComparator()));
    }

    /**
     * Stores all visited {@link StructuredElement}s.
     * 
     * @see com.top_logic.element.core.TLElementVisitor#onVisit(StructuredElement, int)
     */
    @Override
	public boolean onVisit(StructuredElement anElement, int aDepth) {
        if (anElement instanceof StructuredElement) {
            StructuredElement sElement = (StructuredElement) anElement;
            if (this.collected.contains(sElement)) {
                return (false); // Found duplicate ?
            }
            else {
                StructuredElement theParent = sElement.getParent();

                // Check if sElement is really a child of startSE,
                while ((theParent != this.startSE) && (theParent != null)) {
                    if (this.collected.contains(theParent)) {
                        this.collected.add(sElement);

                        return (true);
                    }
                    else {
                        theParent = theParent.getParent();
                    }
                }

                if (theParent == this.startSE) {
                    this.collected.add(sElement);
                }

                return (true);
            }
        }
        
        return (false);
    }

    /**
     * Return the collected Elements.
     */
    public Set<StructuredElement> getCollected() {
        return collected;
    }
}
