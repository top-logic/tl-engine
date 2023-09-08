/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.util.TLModelUtil;

/**
 * Transformation that removes abstract classes without concrete specializations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RemoveUnusedAbstractClasses extends ModelTransformation {

	/**
	 * Creates a {@link RemoveUnusedAbstractClasses} transformation.
	 *
	 * @param log
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 * @param index
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 */
	public RemoveUnusedAbstractClasses(Protocol log, TLModel index) {
		super(log, index);
	}

	@Override
	public void transform() {
		// Bring all classes into inverse topological order (specializations first).
		List<TLClass> allAbstractClasses =
			CollectionUtil.topsort(TLModelUtil.GET_GENERALIZATIONS, TLModelUtil.getAllGlobalClasses(index), true);
		FilterUtil.filterInline(TLModelUtil.IS_ABSTRACT, allAbstractClasses);
		Collections.reverse(allAbstractClasses);

		// Copy all local class parts of concrete classes to all concrete specializations.
		for (TLClass clazz : allAbstractClasses) {
			if (TLModelUtil.getTransitiveSpecializations(TLModelUtil.IS_CONCRETE, clazz).isEmpty()) {
				removeClass(clazz);
			}
		}
	}

}
