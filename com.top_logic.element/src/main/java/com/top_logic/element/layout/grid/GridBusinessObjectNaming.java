/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.UnrecordableNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} resolving the name of a {@link GridComponent grid row} and determines
 * the actual business object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GridBusinessObjectNaming
		extends UnrecordableNamingScheme<Object, GridBusinessObjectNaming.GridBusinessObjectName> {

	/**
	 * {@link ModelName} for the business object of a grid row.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Label("Grid business object name")
	public interface GridBusinessObjectName extends ModelName {

		/**
		 * {@link ModelName} for a row in a {@link GridComponent}.
		 */
		@Mandatory
		ModelName getGridRow();

		/**
		 * Setter for {@link #getGridRow()}.
		 */
		void setGridRow(ModelName row);
	}

	/**
	 * Creates a new {@link GridBusinessObjectNaming}.
	 */
	public GridBusinessObjectNaming() {
		super(Object.class, GridBusinessObjectName.class);
	}

	@Override
	public Object locateModel(ActionContext context, GridBusinessObjectName name) {
		Object internalGridRow = ModelResolver.locateModel(context, name.getGridRow());
		return GridUtil.getBusinessObjectFromInternalRow(internalGridRow);
	}

	/**
	 * Creates a {@link GridBusinessObjectName} using the given row name.
	 */
	public static GridBusinessObjectName newName(ModelName gridRowName) {
		GridBusinessObjectName name = TypedConfiguration.newConfigItem(GridBusinessObjectName.class);
		name.setGridRow(gridRowName);
		return name;
	}

}

