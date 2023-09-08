/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.function.part;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link Function1} computing the {@link TLStructuredTypePart#isMultiple() multiplicity} of an
 * {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CombinedPartMultiplicity extends Function2<Boolean, TLStructuredTypePart, Boolean> {

	/** The {@link CombinedPartMultiplicity} instance. */
	public static final CombinedPartMultiplicity INSTANCE = new CombinedPartMultiplicity();

	@Override
	public Boolean apply(TLStructuredTypePart part, Boolean isInputMultiple) {
		if ((part == null) || (isInputMultiple == null)) {
			return null;
		}
		return isInputMultiple || part.isMultiple();
	}

}
