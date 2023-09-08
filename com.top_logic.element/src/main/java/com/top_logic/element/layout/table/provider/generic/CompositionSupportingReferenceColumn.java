/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.provider.generic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.MetaControlProvider;
import com.top_logic.element.meta.form.controlprovider.CompositionControlProvider;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.provider.ReferenceColumn;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link ReferenceColumn} supporting {@link AttributeOperations#isComposition(TLTypePart)
 * composition} references.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompositionSupportingReferenceColumn extends ReferenceColumn {

	/**
	 * Creates a new {@link CompositionSupportingReferenceColumn}.
	 */
	public CompositionSupportingReferenceColumn(TLTypeContext contentType, ResKey headerI18NKey,
			DisplayMode visibility, Accessor accessor) {
		super(contentType, headerI18NKey, visibility, accessor);
	}

	@Override
	protected ControlProvider getControlProvider() {
		if (getTypeContext().isCompositionContext()) {
			return CompositionControlProvider.INSTANCE;
		}
		return MetaControlProvider.INSTANCE;
	}

	@Override
	protected ReferenceColumn createColumn(TLTypeContext type) {
		return new CompositionSupportingReferenceColumn(type, getHeaderI18NKey(), getVisibility(),
			getAccessor());
	}


}

