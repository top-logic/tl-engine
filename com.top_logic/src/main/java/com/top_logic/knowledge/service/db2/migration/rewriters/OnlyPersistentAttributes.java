/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.convert.AbstractValueConverter;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;

/**
 * {@link RewritingEventVisitor} removing all values for attributes for which no database
 * persistence exists.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OnlyPersistentAttributes extends AbstractValueConverter {

	@Override
	protected void translateValues(ItemChange event, Map<String, Object> values) {
		MetaObject objectType = event.getObjectType();
		if (!(objectType instanceof MOStructure)) {
			// Only structures have attributes.
			return;
		}

		MOStructure structure = (MOStructure) objectType;
		Iterator<String> keySet = values.keySet().iterator();
		while (keySet.hasNext()) {
			String attributeName = keySet.next();
			MOAttribute attribute = structure.getAttributeOrNull(attributeName);
			if (attribute == null) {
				// flex attribute, always persistent
				continue;
			}
			if (attribute.getDbMapping().length == 0) {
				// no persistence for attribute, no migration necessary.
				keySet.remove();
			}
		}
	}
}

