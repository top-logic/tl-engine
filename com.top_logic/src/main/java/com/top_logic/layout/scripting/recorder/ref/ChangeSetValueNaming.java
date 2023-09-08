/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.util.Collections;
import java.util.Map;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;

/**
 * {@link ValueNamingScheme} for {@link ChangeSet}.
 * 
 * <p>
 * This {@link ValueNamingScheme} is needed to identify {@link ChangeSet}s via the revision, i.e.
 * the content of the {@link ChangeSet} is not regarded. To use this {@link ChangeSetValueNaming}
 * you must deactivate {@link ChangeSetNaming}.
 * </p>
 * 
 * @see ChangeSetNaming
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetValueNaming extends ValueNamingScheme<ChangeSet> {

	KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	@Override
	public Class<ChangeSet> getModelClass() {
		return ChangeSet.class;
	}

	@Override
	public Map<String, Object> getName(ChangeSet model) {
		Revision revision = kb().getHistoryManager().getRevision(model.getRevision());
		return Collections.<String, Object> singletonMap("revision", revision);
	}

	@Override
	public boolean matches(Map<String, Object> name, ChangeSet model) {
		return matchesDefault(name, model);
	}

}

