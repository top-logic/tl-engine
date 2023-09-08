/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Accessor used to transform {@link CompareRowObject} to {@link CompareInfo}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareTableAccessor extends ReadOnlyAccessor<CompareRowObject> {

	private final Accessor<Object> _origAccessor;

	private final CompareService<? extends CompareInfo> _compareService;

	/**
	 * Creates a new {@link CompareTableAccessor}.
	 * 
	 * @param origAccessor
	 *        {@link Accessor} to resolve column value from {@link CompareRowObject#changeValue()} and
	 *        {@link CompareRowObject#baseValue()}.
	 * @param compareService
	 *        The {@link CompareService} used for comparison.
	 */
	public CompareTableAccessor(Accessor<Object> origAccessor, CompareService<? extends CompareInfo> compareService) {
		_origAccessor = origAccessor;
		_compareService = compareService;
	}

	@Override
	public Object getValue(CompareRowObject object, String property) {
		Object baseValue = object.baseValue();
		if (baseValue != null) {
			baseValue = _origAccessor.getValue(baseValue, property);
		}
		Object changeValue = object.changeValue();
		if (changeValue != null) {
			changeValue = _origAccessor.getValue(changeValue, property);
		}

		if (baseValue instanceof List && changeValue instanceof List) {
			return _compareService.newCollectionCompareInfo((List<?>) baseValue, (List<?>) changeValue,
				MetaResourceProvider.INSTANCE);
		}
		if (baseValue instanceof Collection && changeValue instanceof Collection) {
			return _compareService.newCollectionCompareInfo(CollectionUtil.toList((Collection<?>) baseValue),
				CollectionUtil.toList((Collection<?>) changeValue), MetaResourceProvider.INSTANCE);
		}
		return _compareService.newCompareInfo(baseValue, changeValue);
	}

}

