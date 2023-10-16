/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLVisibility;

/**
 * {@link GridComponent} for the generic instances table in the administration.
 * <p>
 * It overrides the {@link TLVisibility} from the model and displays every column as editable,
 * unless it is technically impossible (e.g. derived columns).
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class InstanceBrowserGrid extends GridComponent {

	/** {@link TypedConfiguration} constructor for {@link InstanceBrowserGrid}. */
	public InstanceBrowserGrid(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean isHiddenInCreate(TLStructuredTypePart attribute) {
		/* Don't make attributes visible during the creation: The value of some attributes is
		 * computed during the creation. When a field would be built for such an attribute, the
		 * (empty) field value would always override the computed value. */
		return super.isHiddenInCreate(attribute);
	}

	@Override
	protected boolean isHidden(TLStructuredTypePart attribute) {
		return false;
	}

	@Override
	protected AttributeUpdate modifyUpdateForAdd(String name, TLStructuredTypePart attribute, TLObject row,
			AttributeUpdate update) {
		if (update.isDisabled() && !update.isDerived()) {
			update.setDisabled(false);
		}
		return super.modifyUpdateForAdd(name, attribute, row, update);
	}

}
