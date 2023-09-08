/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.resources;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.visit.LabelVisitor;

/**
 * {@link ReadOnlyAccessor} accessing the label of a {@link TLModelPart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModelPartLabelAccessor extends ReadOnlyAccessor<TLModelPart> {

	@Override
	public Object getValue(TLModelPart object, String property) {
		return object.visit(LabelVisitor.INSTANCE, null);
	}

}

