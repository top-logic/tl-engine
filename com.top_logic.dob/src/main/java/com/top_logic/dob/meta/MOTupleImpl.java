/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.List;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;

/**
 * The default {@link MOTuple} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MOTupleImpl extends AbstractMetaObject implements MOTuple {

	private final List<MetaObject> entryTypes;

	/**
	 * Creates a {@link MOTupleImpl}.
	 * 
	 * @param entryTypes
	 *        See {@link #getEntryTypes()}.
	 */
	public MOTupleImpl(List<MetaObject> entryTypes) {
		super(createName(entryTypes));
		
		this.entryTypes = entryTypes;
	}
	
	private static String createName(List<MetaObject> entryTypes) {
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int n = 0, cnt = entryTypes.size(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(',');
			}
			buffer.append(entryTypes.get(n).getName());
		}
		buffer.append(')');
		return buffer.toString();
	}

	@Override
	public final Kind getKind() {
		return Kind.tuple;
	}
	
	@Override
	public List<MetaObject> getEntryTypes() {
		return entryTypes;
	}

	@Override
	public MetaObject copy() {
		return new MOTupleImpl(typesRef(entryTypes));
	}
	
	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		resolveTypes(context, entryTypes);
		return this;
	}
	
}
