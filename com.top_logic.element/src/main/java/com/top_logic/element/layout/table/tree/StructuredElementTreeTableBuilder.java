/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.tree;


import com.top_logic.element.layout.grid.StructuredElementTreeGridModelBuilder;
import com.top_logic.element.layout.structured.StructuredElementTreeBuilder;
import com.top_logic.element.layout.structured.StructuredElementTreeModelBuilder;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * An {@link DefaultTreeTableBuilder} for {@link StructuredElement}s as
 * {@link DefaultTreeTableNode#getBusinessObject() business objects}.
 * 
 * @see StructuredElementTreeBuilder
 * @see StructuredElementTreeModelBuilder
 * @see StructuredElementTreeGridModelBuilder
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class StructuredElementTreeTableBuilder extends AbstractStructuredElementTreeTableBuilder {

	/** The singleton instance of the {@link StructuredElementTreeTableBuilder}. */
	public static final StructuredElementTreeTableBuilder INSTANCE = new StructuredElementTreeTableBuilder();

	private StructuredElementTreeTableBuilder() {
		// Reduce visibility
	}

}
