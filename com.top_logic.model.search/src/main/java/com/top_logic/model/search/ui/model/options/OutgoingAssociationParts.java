/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.CollectionUtil.*;
import static com.top_logic.model.search.ui.model.options.SearchTypeUtil.*;
import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLStructuredType;

/**
 * Option provider that returns the outgoing {@link TLAssociationPart}s for a given incoming
 * {@link TLAssociationPart}.
 * <p>
 * The return type is {@link TLAssociationPart} and not {@link TLAssociationEnd}, as not only
 * {@link TLAssociationEnd}s can point to a {@link TLStructuredType}, but also
 * {@link TLAssociationProperty}s, too.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class OutgoingAssociationParts extends Function2<List<TLAssociationPart>, TLAssociationPart, String> {

	/** The {@link OutgoingAssociationParts} instance. */
	public static final OutgoingAssociationParts INSTANCE = new OutgoingAssociationParts();

	@Override
	public List<TLAssociationPart> apply(TLAssociationPart incomingPart, String configName) {
		if (containsNull(incomingPart, configName)) {
			return emptyList();
		}
		return applyModelFilter(getOtherEnds(incomingPart), configName);
	}

}
