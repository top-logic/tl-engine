/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.factory.TLFactory;

/**
 * Default factory for non-structure types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultModelFactory extends ModelFactory {

	@Override
	public TLObject createObject(TLClass type, TLObject context, ValueProvider initialValues, TLID id) {
		NameValueBuffer values = new NameValueBuffer();
		values.setValue(PersistentObject.TYPE_REF, type.tHandle());

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		String tableName = TLAnnotations.getTable(type);
		TLFactory.failIfAbstract(type);
		TLObject result = kb.createObject(kb.getHistoryManager().getContextBranch(), id, tableName, values);
		TLFactory.setupDefaultValues(context, result, type);

		return result;
	}

}
