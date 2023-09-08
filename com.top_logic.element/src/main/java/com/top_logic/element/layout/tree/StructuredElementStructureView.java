/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.tree;

import java.util.Iterator;

import com.top_logic.basic.col.StructureView;
import com.top_logic.element.structured.StructuredElement;

/**
 * {@link StructureView} encapsulating a {@link StructuredElement} hierarchy.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredElementStructureView implements StructureView<StructuredElement> {

	/**
	 * Singleton {@link StructuredElementStructureView} instance.
	 */
	public static final StructuredElementStructureView INSTANCE = new StructuredElementStructureView();

	private StructuredElementStructureView() {
		// Singleton constructor.
	}

	@Override
	public boolean isLeaf(StructuredElement node) {
		return node.getChildren().isEmpty();
	}

	@Override
	public Iterator<? extends StructuredElement> getChildIterator(StructuredElement node) {
		return node.getChildren().iterator();
	}

	@Override
	public StructuredElement getParent(StructuredElement node) {
		return node.getParent();
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}
