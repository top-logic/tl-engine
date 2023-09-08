/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.util.Collection;

import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} retrieving all types in a {@link SchemaConfiguration} model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBTablesListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link DBTablesListModelBuilder} instance.
	 */
	public static final DBTablesListModelBuilder INSTANCE = new DBTablesListModelBuilder();

	private DBTablesListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return ((SchemaConfiguration) businessModel).getMetaObjects().getTypes().values();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof SchemaConfiguration;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof MetaObjectName;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return contextComponent.getModel();
	}

}
