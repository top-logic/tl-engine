/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.xref.AllEnumerations;

/**
 * {@link ModelTransformation} that removes {@link TLEnumeration}s that are not
 * referenced by any {@link TLTypePart}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RemoveUnusedEnumerations extends ModelTransformation {

	/**
	 * Creates a {@link RemoveUnusedEnumerations} transformation.
	 *
	 * @param log
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 * @param index
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 */
	public RemoveUnusedEnumerations(Protocol log, TLModel index) {
		super(log, index);
	}

	@Override
	public void transform() {
		Set<TLEnumeration> enumerations = AllEnumerations.findAllEnumerations(index);
		
		// Filter out used enumerations.
		for (Iterator<TLEnumeration> it = enumerations.iterator(); it.hasNext(); ) {
			TLEnumeration enumeration = it.next();
			if (! TLModelUtil.getUsage(index, enumeration).isEmpty()) {
				it.remove();
			}
		}
		
		for (TLEnumeration enumeration : enumerations) {
			removeEnumeration(enumeration);
		}
	}

}
