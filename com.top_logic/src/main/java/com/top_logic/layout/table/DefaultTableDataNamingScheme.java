/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.table.component.DefaultTableDataName;

/**
 * The {@link ModelNamingScheme} for {@link DefaultTableData} instances relies on the
 * {@link TableDataOwner} retrievable via {@link DefaultTableData#getOwner()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DefaultTableDataNamingScheme extends AbstractModelNamingScheme<DefaultTableData, DefaultTableDataName> {

	/**
	 * The instance of the {@link DefaultTableDataNamingScheme}. This is not a
	 * singleton, as (potential) subclasses can create further instances.
	 */
	public static final DefaultTableDataNamingScheme INSTANCE =
		new DefaultTableDataNamingScheme();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected DefaultTableDataNamingScheme() {
		// See JavaDoc above.
	}

	@Override
	public Class<DefaultTableDataName> getNameClass() {
		return DefaultTableDataName.class;
	}

	@Override
	public Class<DefaultTableData> getModelClass() {
		return DefaultTableData.class;
	}

	@Override
	public DefaultTableData locateModel(ActionContext context, DefaultTableDataName name) {
		TableDataOwner tableDataOwner =
			(TableDataOwner) ModelResolver.locateModel(context, name.getDefaultTableDataOwner());
		ApplicationAssertions.assertNotNull(name, "Table data owner cannot be resolved", tableDataOwner);
		return (DefaultTableData) tableDataOwner.getTableData();
	}

	@Override
	protected void initName(DefaultTableDataName name, DefaultTableData model) {
		name.setDefaultTableDataOwner(model.getOwner().getModelName());
	}

}
