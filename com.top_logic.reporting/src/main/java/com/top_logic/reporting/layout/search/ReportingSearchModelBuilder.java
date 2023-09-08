/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.search;

import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.layout.meta.search.SearchModelBuilder;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.util.error.TopLogicException;

/**
 * TODO tbe, fsc: erweitern auf structuredElementModelBuilder ?
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ReportingSearchModelBuilder implements SearchModelBuilder {

	/**
	 * Singleton {@link ReportingSearchModelBuilder} instance.
	 */
	public static final ReportingSearchModelBuilder INSTANCE = new ReportingSearchModelBuilder();

	private ReportingSearchModelBuilder() {
		// Singleton constructor.
	}

	/**
	 * @see com.top_logic.element.layout.meta.search.SearchModelBuilder#getMetaElement(java.lang.String)
	 */
	@Override
	public TLClass getMetaElement(String aMetaElement) throws IllegalArgumentException {
		StructuredElement theRoot = this.getStructureRoot();

		if (theRoot instanceof MetaElementHolder) {
			return ((MetaElementHolder) theRoot).getMetaElement(aMetaElement);
		}
		else {
			throw new TopLogicException(this.getClass(), "me.get.holder", new String[] {aMetaElement});
		}
	}

	/**
	 * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		StructuredElement theRoot    = this.getStructureRoot();
		AllElementVisitor theVisitor = new AllElementVisitor();

		TraversalFactory.traverse(theRoot, theVisitor, TraversalFactory.DEPTH_FIRST);

		return theVisitor.getList();
	}

	/**
	 * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof TLClass;
	}

	protected StructuredElement getStructureRoot() {
		return StructuredElementFactory.getInstanceForStructure("projElement").getRoot();
	}

}

