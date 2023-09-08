/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;

/**
 * Option provider that returns the incoming {@link TLAssociationPart}s for a given {@link TLType}.
 * <p>
 * The return type is {@link TLAssociationPart} and not {@link TLAssociationEnd}, as not only
 * {@link TLAssociationEnd}s can point to a {@link TLStructuredType}, but also
 * {@link TLAssociationProperty}s, too.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class IncomingAssociationParts extends Function2<List<TLAssociationPart>, TLType, String> {

	/** The {@link IncomingAssociationParts} instance. */
	public static final IncomingAssociationParts INSTANCE = new IncomingAssociationParts();

	@Override
	public List<TLAssociationPart> apply(TLType contextType, String configName) {
		if (containsNull(contextType, configName)) {
			return emptyList();
		}
		if (!(contextType instanceof TLClass)) {
			return emptyList();
		}
		return SearchTypeUtil.getPartsPointingTo((TLClass) contextType, configName);
	}

}
