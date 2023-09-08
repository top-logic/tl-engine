/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances;

import java.util.Collection;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * {@link ListModelBuilder} retrieving all instances of it's component's model (assuming it is a
 * {@link TLClass}).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectInstances implements ListModelBuilder {

	/**
	 * Singleton {@link DirectInstances} instance.
	 */
	public static final DirectInstances INSTANCE = new DirectInstances();

	private DirectInstances() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return MetaElementUtil.getAllDirectInstancesOf((TLClass) businessModel, TLObject.class);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof TLClass;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		if (!(listElement instanceof TLObject)) {
			return false;
		}
		TLStructuredType tType = ((TLObject) listElement).tType();
		if (tType == null) {
			// internal type
			return false;
		}
		return tType.getModelKind() == ModelKind.CLASS;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return ((TLObject) listElement).tType();
	}

}
