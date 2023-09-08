/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.function.part;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * {@link Function1} computing the {@link TLStructuredType} of an {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PartType extends Function1<TLType, TLStructuredTypePart> {

	/** The {@link PartType} instance. */
	public static final PartType INSTANCE = new PartType();

	@Override
	public TLType apply(TLStructuredTypePart arg) {
		if (arg == null) {
			return null;
		}
		return arg.getType();
	}

}