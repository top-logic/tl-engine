/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.action.SetGlobalVariableAction;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for the last committed revision.
 * 
 * @see LastRevisionNamingScheme.Name
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LastRevisionNamingScheme extends AbstractModelNamingScheme<Revision, LastRevisionNamingScheme.Name> {

	/**
	 * {@link ModelNamingScheme} for the last committed revision.
	 * 
	 * <p>
	 * Note: The result of resolving this {@link ModelName} changes over time. The results depends
	 * on the state of the {@link PersistencyLayer} of the system. It is recommended to use this
	 * {@link ModelName} in combination with a {@link SetGlobalVariableAction}. This allows to refer
	 * to the state of the system at the time, the global variable was assigned. The concrete
	 * Revision can then be referenced through a {@link GlobalVariableRef}.
	 * </p>
	 * 
	 * @see LastRevisionNamingScheme
	 */
	public interface Name extends ModelName {
		// Pure marker interface.
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<Revision> getModelClass() {
		return Revision.class;
	}

	@Override
	public Revision locateModel(ActionContext context, Name name) {
		HistoryManager hm = PersistencyLayer.getKnowledgeBase().getHistoryManager();
		long commitNumber = hm.getLastRevision();
		return hm.getRevision(commitNumber);
	}

	@Override
	protected boolean isCompatibleModel(Revision model) {
		// Do not use this naming scheme to build stable names for existing revisions. This naming
		// scheme must only be used to resolve the last committed revision, if the name was
		// explicitly inserted into a test script.
		return false;
	}

	@Override
	protected void initName(Name name, Revision model) {
		throw new UnreachableAssertion("This naming scheme must not be used to build stable names for revisions.");
	}

}
