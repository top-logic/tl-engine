/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.ArrayUtil;

/**
 * {@link Values} implementation that represents deleted data for a given revision range.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeletedValues extends ValuesImpl {

	/**
	 * Creates a new {@link DeletedValues}.
	 * 
	 * @param minValidity
	 *        The revision in which the data are deleted.
	 * @param maxValidity
	 *        The revision in which the data are still deleted.
	 */
	public DeletedValues(long minValidity, long maxValidity) {
		super(minValidity, maxValidity, ArrayUtil.EMPTY_ARRAY);
	}

	@Override
	public Object[] getData() {
		throw deletedObjectAccess();
	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder tmp = new StringBuilder();
		tmp.append("Deleted[max:");
		tmp.append(maxValidity());
		tmp.append(",min:");
		tmp.append(minValidity());
		if (formerValidity() != null) {
			tmp.append(",older:");
			tmp.append(formerValidity());
		}
		tmp.append("]");
		return tmp.toString();
	}

}

