/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link LabelProvider} for {@link TLType}s dispatching to {@link MetaResourceProvider} and adding
 * {@link TLScope} information for local types.
 * 
 * @see MetaResourceProvider
 * @see LabelProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLTypeLabelProvider extends TypeSafeLabelProvider<TLType> {

	@Override
	protected Class<TLType> getObjectType() {
		return TLType.class;
	}

	@Override
	protected String getNonNullLabel(TLType type) {
		String typeLabel = MetaResourceProvider.INSTANCE.getLabel(type);

		if (!TLModelUtil.isGlobal(type)) {
			return MetaResourceProvider.INSTANCE.getLabel(type.getScope()) + " " + typeLabel;
		}

		return typeLabel;
	}

}
