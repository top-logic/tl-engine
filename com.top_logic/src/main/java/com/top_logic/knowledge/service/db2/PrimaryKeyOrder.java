/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Comparator;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOIndex;

/**
 * Holder for comparators that compares items first by type and then by the values of the primary
 * key.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class PrimaryKeyOrder {

	/**
	 * Comparator that compares by using the global values of the primary key.
	 */
	public static final Comparator<AbstractDBKnowledgeItem> GLOBAL_VALUE_COMPARATOR = new GlobalValueComparator();

	/**
	 * Comparator that compares by using the local values of the primary key.
	 */
	public static final Comparator<AbstractDBKnowledgeItem> LOCAL_VALUE_COMPARATOR = new LocalValueComparator();

	private abstract static class PrimaryKeyComparator implements Comparator<AbstractDBKnowledgeItem> {

		PrimaryKeyComparator() {
		}

		@Override
		public int compare(AbstractDBKnowledgeItem o1, AbstractDBKnowledgeItem o2) {
			MOKnowledgeItem mo1 = o1.tTable();
			MOKnowledgeItem mo2 = o2.tTable();
			if (mo1 != mo2) {
				String typeName1 = mo1.getName();
				String typeName2 = mo2.getName();

				int typeResult = typeName1.compareTo(typeName2);
				if (typeResult != 0) {
					return typeResult;
				}
			}

			MOIndex index = mo1.getPrimaryKey();
			for (MOAttribute indexAttr : index.getAttributes()) {
				// TODO: this does not work with values not comparable, e.g. reference values
				Comparable value1 = (Comparable) getValue(o1, indexAttr);
				Object value2 = getValue(o2, indexAttr);
				
				int compareResult = value1.compareTo(value2);
				if (compareResult != 0) {
					return compareResult;
				}
			}
			return 0;
		}

		protected abstract Object getValue(AbstractDBKnowledgeItem item, MOAttribute attribute);

	}

	private static class GlobalValueComparator extends PrimaryKeyComparator {

		GlobalValueComparator() {
		}

		@Override
		protected Object getValue(AbstractDBKnowledgeItem item, MOAttribute attribute) {
			return attribute.getStorage().getApplicationValue(attribute, item, item, item.getGlobalValues());
		}

	}

	private static class LocalValueComparator extends PrimaryKeyComparator {

		LocalValueComparator() {
		}

		@Override
		protected Object getValue(AbstractDBKnowledgeItem item, MOAttribute attribute) {
			return attribute.getStorage().getApplicationValue(attribute, item, item, item.getLocalValues());
		}

	}

}

