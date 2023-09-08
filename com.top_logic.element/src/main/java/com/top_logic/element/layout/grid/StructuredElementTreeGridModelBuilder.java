/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.layout.structured.DefaultStructuredElementTreeModelBuilder;
import com.top_logic.element.layout.structured.StructuredElementTreeBuilder;
import com.top_logic.element.layout.structured.StructuredElementTreeModelBuilder;
import com.top_logic.element.layout.table.tree.StructuredElementTreeTableBuilder;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TreeModelBuilder} that creates a tree from the given {@link StructuredElement}.
 * <p>
 * To be used in the TreeGrid as
 * {@link com.top_logic.element.layout.grid.GridComponent.Config#getModelBuilder() model builder}.
 * </p>
 * 
 * @see StructuredElementTreeTableBuilder
 * @see StructuredElementTreeBuilder
 * @see StructuredElementTreeModelBuilder
 * 
 * @deprecated Use {@link DefaultStructuredElementTreeModelBuilder}.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
@Deprecated
public class StructuredElementTreeGridModelBuilder extends AbstractTreeModelBuilder<StructuredElement> {

	/**
	 * Singleton {@link StructuredElementTreeGridModelBuilder} instance.
	 */
	public static final StructuredElementTreeGridModelBuilder INSTANCE = new StructuredElementTreeGridModelBuilder();

	private StructuredElementTreeGridModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, StructuredElement node) {
		return node;
	}

	@Override
	public Collection<? extends StructuredElement> getParents(LayoutComponent contextComponent, StructuredElement node) {
		StructuredElement parent = node.getParent();
		if (parent == null) {
			// node is root
			return Collections.emptyList();
		} else {
			return Collections.singleton(parent);
		}
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return (node instanceof StructuredElement);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof StructuredElement;
	}

	@Override
	public Iterator<? extends StructuredElement> getChildIterator(StructuredElement node) {
		return CollectionUtil.dynamicCastView(StructuredElement.class, node.getChildren()).iterator();
	}

}
