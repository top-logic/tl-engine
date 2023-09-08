/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.form.model.FormGroup;

/**
 * {@link Accessor} to the row group of the tree table node.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class RowGroupColumnAccessor extends ReadOnlyAccessor<Object> {
	
	/** {@link Property} to access the {@link FormGroup} created for the node. */
	public static final Property<FormGroup> ROW_GROUP = TypedAnnotatable.property(FormGroup.class, "row group");

	@Override
	public Object getValue(Object object, String property) {
		TypedAnnotatable node = (TypedAnnotatable) object;
		FormGroup formGroup = node.get(ROW_GROUP);
		if (formGroup == null) {
			return null;
		}
		if (formGroup.hasMember(property)) {
			return formGroup.getMember(property);
		}
		return null;
	}
}
