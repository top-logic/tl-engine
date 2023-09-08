/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.util.Utils;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ui.model.AttributeFilter;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Option provider for {@link AttributeFilter#getAttribute()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class AllParts extends Function2<List<TLStructuredTypePart>, TLType, String> {

	/** The {@link AllParts} instance. */
	public static final AllParts INSTANCE = new AllParts();

	@Override
	public List<TLStructuredTypePart> apply(TLType contextType, String configName) {
		if (containsNull(contextType, configName)) {
			return emptyList();
		}
		List<TLStructuredTypePart> result = new ArrayList<>();
		if (contextType instanceof TLAssociation) {
			TLAssociation type = (TLAssociation) contextType;
			result.addAll(type.getAssociationParts());
		}
		else if (contextType instanceof TLClass) {
			TLClass type = (TLClass) contextType;
			result.addAll(TLModelUtil.getAllProperties(type));
			result.addAll(TLModelUtil.getAllReferences(type));
		} else {
			throw new UnreachableAssertion("Unexpected " + TLStructuredType.class.getSimpleName() + ":"
				+ Utils.debug(contextType));
		}
		return ModelBasedSearch.getInstance().filterModel(result, configName);
	}

}
