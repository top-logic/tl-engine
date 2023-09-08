/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.util.TLModelUtil;

/**
 * Generator that returns all elements of a structure
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class TLElementGenerator extends ListGeneratorAdaptor {
	
	private final String structureName;
	
	public TLElementGenerator(String aStructureName) {
		if (StringServices.isEmpty(aStructureName)) {
			throw new IllegalArgumentException("Must specify a structure name");
		}
		
		this.structureName = aStructureName;

	}

	/**
	 * Get all elements of the configured structure via a visitor.
	 * 
	 * @see com.top_logic.element.meta.kbbased.filtergen.Generator#generate(EditContext)
	 */
	@Override
	public List<?> generateList(EditContext editContext) {
		
		StructuredElement theRoot = (StructuredElement) TLModelUtil.findSingleton(getStructureName());
		
        AllElementVisitor theVisitor = new AllElementVisitor();
        TraversalFactory.traverse(theRoot, theVisitor, TraversalFactory.DEPTH_FIRST);

		return theVisitor.getList();
	}

	public String getStructureName() {
		return structureName;
	}

}
