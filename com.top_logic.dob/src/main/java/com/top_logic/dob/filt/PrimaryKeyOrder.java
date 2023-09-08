/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import java.util.Comparator;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOStructure;

/**
 * Order that sorts {@link DataObject}s according to their type name and then
 * according to their {@link MOStructure#getPrimaryKey()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class PrimaryKeyOrder implements Comparator<DataObject> {

	/**
	 * Singleton {@link PrimaryKeyOrder} instance.
	 */
	public static final PrimaryKeyOrder INSTANCE = new PrimaryKeyOrder();

	private PrimaryKeyOrder() {
		// Singleton constructor.
	}

	@Override
	public int compare(DataObject o1, DataObject o2) {
		try {
			MOStructure mo1 = (MOStructure) o1.tTable();
			MOStructure mo2 = (MOStructure)o2.tTable();
			String typeName1 = mo1.getName();
			String typeName2 = mo2.getName();
			int typeResult = typeName1.compareTo(typeName2);
			if (typeResult != 0) {
				return typeResult;
			}
			
			MOIndex index = mo1.getPrimaryKey();
			for (MOAttribute indexAttr : index.getAttributes()) {
				// TODO: this does not work with values not comparable, e.g. reference values
				String indexAttrName = indexAttr.getName();
				Comparable value1 = (Comparable) o1.getAttributeValue(indexAttrName);
				Object value2 = o2.getAttributeValue(indexAttrName);
				
				int compareResult = value1.compareTo(value2);
				if (compareResult != 0) {
					return compareResult;
				}
			}
			return 0;
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
	}
}