/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core;

import com.top_logic.basic.graph.TraversalListener;
import com.top_logic.element.structured.StructuredElement;


/**
 * Interface used for traversing a {@link StructuredElement} structure.
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public interface TLElementVisitor extends TraversalListener<StructuredElement> {

	/**
	 * This method is called by the visitor (which is the {@link StructuredElement}).
	 * 
	 * Depending on the type of visiting, the traverse of the elements will
	 * differ. Each implementation is free to read (not to modify!) the
	 * {@link StructuredElement} information.
	 * 
	 * @param anElement
	 *        Element currently visited by
	 *        {@link TraversalFactory#traverse(StructuredElement, TLElementVisitor, int, boolean, int)}.
	 * @param aDepth
	 *        Depth of anElement relative to the root element on which visiting
	 *        has started.
	 * @return <code>true</code> to indicate, that the travering has to be
	 *         continued, if <code>false</code>, the traversing will stop at
	 *         this element.
	 */
   @Override
public boolean onVisit(StructuredElement anElement, int aDepth);
}