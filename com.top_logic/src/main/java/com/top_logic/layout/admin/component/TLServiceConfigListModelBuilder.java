/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Collection;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} creating a collection of all {@link BasicRuntimeModule}s as model for a
 * {@link TableComponent}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLServiceConfigListModelBuilder implements ListModelBuilder {

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return ModuleUtil.getAllModules();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof BasicRuntimeModule<?>;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return contextComponent.getModel();
	}

}
