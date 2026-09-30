/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import java.util.Collection;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.layout.grid.NewObject;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * A {@link ModelNamingScheme} that identifies a grid component row with an object being created.
 */
public class GridNewObjectNaming
		extends ModelNamingScheme<GridComponent, NewObject, GridNewObjectNaming.GridNewObjectName> {

	/** {@link ModelName} for the {@link GridNewObjectNaming}. */
	public interface GridNewObjectName extends ModelName {
		// Pure marker interface.
	}

	/** Creates a {@link GridNewObjectNaming}. */
	public GridNewObjectNaming() {
		super(NewObject.class, GridNewObjectName.class, GridComponent.class);
	}

	@Override
	public NewObject locateModel(ActionContext context, GridComponent valueContext, GridNewObjectName name) {
		Object selected = valueContext.getSelected();

		Object element;
		if (selected instanceof Collection<?> collection) {
			if (collection.size() == 1) {
				element = collection.iterator().next();
			} else {
				return null;
			}
		} else {
			element = selected;
		}

		if (element instanceof NewObject result) {
			return result;
		}

		return null;
	}

	@Override
	protected Maybe<GridNewObjectName> buildName(GridComponent valueContext, NewObject model) {
		return Maybe.some(TypedConfiguration.newConfigItem(GridNewObjectName.class));
	}

}
