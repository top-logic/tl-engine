/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;

/**
 * The {@link MetaElementTableListModelBuilder} is a {@link ListModelBuilder}
 * for showing {@link TLClass}s in a {@link TableComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MetaElementTableListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link MetaElementTableListModelBuilder} instance.
	 */
	public static final MetaElementTableListModelBuilder INSTANCE = new MetaElementTableListModelBuilder();

	private MetaElementTableListModelBuilder() {
		// Singleton constructor.
	}

	/**
	 * @see com.top_logic.mig.html.ListModelBuilder#retrieveModelFromListElement(com.top_logic.mig.html.layout.LayoutComponent,
	 *      java.lang.Object)
	 */
	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object anObject) {
		assert anObject instanceof TLClass;
		MetaElementHolder theHolder = MetaElementUtil.getMetaElementHolder(((TLClass) anObject));
		return theHolder;
	}

	/**
	 * @see com.top_logic.mig.html.ListModelBuilder#supportsListElement(com.top_logic.mig.html.layout.LayoutComponent,
	 *      java.lang.Object)
	 */
	@Override
	public boolean supportsListElement(LayoutComponent component, Object anObject) {
		return anObject instanceof TLClass;
	}

	/**
	 * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		MetaElementHolder theHolder = (MetaElementHolder) businessModel;

		if (theHolder == null) {
			return new ArrayList(MetaElementFactory.getInstance().getAllMetaElements());
		} else {
			// KBU side effect programming: create meta element...
			((Wrapper) theHolder).tType();
			return new ArrayList<>(theHolder.getMetaElements());
		}
	}

	/**
	 * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object,
	 *      com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model instanceof MetaElementHolder || model == null;
	}

}
