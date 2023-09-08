/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.function.part;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.Utils;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link Function1} computing the {@link TLStructuredTypePart#isMultiple() multiplicity} of an
 * {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PartMultiplicity extends Function1<Boolean, TLStructuredTypePart> {

	/** The {@link PartMultiplicity} instance. */
	public static final PartMultiplicity INSTANCE = new PartMultiplicity();

	@Override
	public Boolean apply(TLStructuredTypePart part) {
		if (part == null) {
			return null;
		}
		if (part instanceof TLClassPart) {
			return part.isMultiple();
		} else if (part instanceof TLAssociationPart) {
			// Associations can always be multiple, as there can always be more than one.
			return true;
		} else {
			throw new UnreachableAssertion("Unexpected " + TLStructuredTypePart.class.getSimpleName() + ":"
				+ Utils.debug(part));
		}
	}

}
