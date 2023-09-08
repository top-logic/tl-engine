/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import java.util.Collection;
import java.util.Iterator;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ProjectTreeBuilder extends AbstractTreeModelBuilder<StructuredElement> {

	/**
	 * Singleton {@link ProjectTreeBuilder} instance.
	 */
	public static final ProjectTreeBuilder INSTANCE = new ProjectTreeBuilder();

	private ProjectTreeBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, StructuredElement node) {
		return null;
	}

	@Override
	public Collection<? extends StructuredElement> getParents(LayoutComponent contextComponent,
			StructuredElement node) {
		return null;
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return node instanceof Object;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof Object;
	}

	@Override
	public Iterator<? extends StructuredElement> getChildIterator(StructuredElement node) {
		return node.getChildren().iterator();
	}


}
