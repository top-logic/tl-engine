/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.func.Function3;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ui.model.QueryValue;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Option provider for {@link QueryValue#getPart()}.
 * <p>
 * Returns the {@link TLStructuredTypePart}s that store a value of the given {@link TLType}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class PartOptions extends Function3<List<TLStructuredTypePart>, TLType, Boolean, String> {

	/** The {@link PartOptions} instance. */
	public static final PartOptions INSTANCE = new PartOptions();

	@Override
	public List<TLStructuredTypePart> apply(TLType contextType, Boolean multiple, String configName) {
		if (containsNull(contextType, multiple, configName)) {
			return emptyList();
		}
		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLType compatibleType : SearchTypeUtil.getCompatibleTypes(contextType)) {
			addUsage(result, compatibleType, multiple);
		}
		return ModelBasedSearch.getInstance().filterModel(result, configName);
	}

	private void addUsage(List<TLStructuredTypePart> result, TLType contextType, boolean multiple) {
		Collection<TLStructuredTypePart> usages = TLModelUtil.getUsage(ModelService.getApplicationModel(), contextType);
		for (TLStructuredTypePart part : usages) {
			if (part.getOwner().getName() == null) {
				// No anonymous types.
				continue;
			}
			if (part.isMultiple() && !multiple) {
				continue;
			}
			if (SearchTypeUtil.isReferenceImpl(part)) {
				/* Filter duplicates where a Reference and its End are both reported as usages. One
				 * of them is enough. */
				continue;
			}
			result.add(part);
		}
	}

}
