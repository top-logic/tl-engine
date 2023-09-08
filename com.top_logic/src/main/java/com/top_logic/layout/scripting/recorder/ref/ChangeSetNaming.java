/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link ChangeSet}.
 * 
 * @see ChangeSetValueNaming
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetNaming extends AbstractModelNamingScheme<ChangeSet, ChangeSetNaming.Name> {

	/**
	 * Name for a {@link ChangeSet}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Name extends ModelName {

		/**
		 * The name for the revision of the represented {@link ChangeSet}.
		 */
		ModelName getRevision();

		/**
		 * Setter for {@link #getRevision()}.
		 * 
		 * @param value
		 *        Value of {@link #getRevision()}.
		 */
		void setRevision(ModelName value);

	}

	@Override
	protected void initName(Name name, ChangeSet model) {
		Revision revision = kb().getHistoryManager().getRevision(model.getRevision());
		name.setRevision(ModelResolver.buildModelName(revision));
	}

	@Override
	public ChangeSet locateModel(ActionContext context, Name name) {
		ModelName revisionName = name.getRevision();
		Revision revision = (Revision) ModelResolver.locateModel(context, revisionName);
		HistoryManager hm = kb().getHistoryManager();
		Revision startRev = revision;
		Revision stopRev = hm.getRevision(revision.getCommitNumber() + 1);
		ReaderConfig readerConfig = ReaderConfigBuilder.createConfig(startRev, stopRev);
		try (ChangeSetReader reader = kb().getChangeSetReader(readerConfig)) {
			return reader.read();
		}
	}

	@Override
	protected boolean isCompatibleModel(ChangeSet model) {
		if (useValueNamingScheme()) {
			return false;
		}
		return super.isCompatibleModel(model);
	}

	/**
	 * Causes the script recorder to use {@link ChangeSetValueNaming} instead of this
	 * {@link ModelNamingScheme}.
	 */
	private boolean useValueNamingScheme() {
		return !false;
	}

	KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	@Override
	public Class<? extends Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<ChangeSet> getModelClass() {
		return ChangeSet.class;
	}

}

