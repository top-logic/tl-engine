/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.Collection;
import java.util.List;

import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;
import com.top_logic.layout.unit.EditUnitComponent;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * This {@link ModelBuilder} constructs a data model (not view model) for
 * the unit table in {@link EditUnitComponent}.
 * 
 * @author     <a href="mailto:teh@top-logic.com">Tobias Ehrler</a>
 */
public class UnitTableModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link UnitTableModelBuilder} instance.
	 */
	public static final UnitTableModelBuilder INSTANCE = new UnitTableModelBuilder();

	private UnitTableModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return null;
	}

	/** 
	 * Overridden to only accept instances of {@link Unit}.
	 * 
	 * @see com.top_logic.mig.html.ListModelBuilder#supportsListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
	 */
	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		return anObject instanceof Unit;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		List<Unit> theModel = UnitWrapper.getAllUnits();
		return theModel;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}
