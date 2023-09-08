/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options.structure;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.operator.ObjectCompare;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * Option provider for {@link ObjectCompare#getCompareObjects()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class RightHandSideOptions extends Function2<List<RightHandSide>, TLType, Boolean> {

	/** The {@link RightHandSideOptions} instance. */
	public static final RightHandSideOptions INSTANCE = new RightHandSideOptions();

	@Override
	public List<RightHandSide> apply(TLType searchedType, Boolean multiplicity) {
		if (containsNull(searchedType, multiplicity)) {
			return emptyList();
		}
		return searchedType.visitType(RightHandSideOptionsVisitor.INSTANCE, multiplicity);
	}

}
