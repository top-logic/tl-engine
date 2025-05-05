/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.Collection;

import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.util.model.ModelService;

/**
 * 
 */
public class ChangeLogListModelBuilder implements ListModelBuilder {

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();

		HistoryManager hm = kb.getHistoryManager();
		long startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24;
		Revision startRev = hm.getRevisionAt(startTime);
		if (startRev.getCommitNumber() < 1) {
			startRev = hm.getRevision(1);
		}

		return new ChangeLogBuilder(kb, model).setStartRev(startRev).build();
	}

	@Override
	public boolean supportsListElement(LayoutComponent component, Object candidate) {
		return candidate instanceof com.top_logic.element.changelog.model.ChangeSet;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object candidate) {
		return null;
	}
}
